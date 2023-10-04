package ws

import (
	"encoding/json"
	"fmt"
	"net/http"
	"talkit-backend/src/sql"

	"github.com/gorilla/websocket"
	"github.com/jmoiron/sqlx"
)

// upgrader：websocket.Upgrader 类型，用于将普通HTTP连接升级为WebSocket连接。
// channelMap：ChannelMap 类型，用于管理用户ID与WebSocket通道的映射。
// stockMap：MsgStockMap 类型，用于存储离线用户的未发送消息。
// db：*sqlx.DB 类型，用于与数据库进行交互。
type MsgHandler struct {
	upgrader   websocket.Upgrader
	channelMap ChannelMap
	stockMap   MsgStockMap
	db         *sqlx.DB
}

// Handle 方法是用于处理WebSocket连接的函数。它接受一个 http.ResponseWriter 和一个 *http.Request，在函数内部进行WebSocket连接的升级，然后处理WebSocket消息。具体操作包括：

// 升级HTTP连接为WebSocket连接。
// 从WebSocket连接接收订阅消息，检查用户身份，然后为用户创建通道。
// 同步离线消息，并将它们发送给用户。
// 启动一个协程，监听通道并将消息发送给客户端。
// 处理用户发送的消息，将其保存到数据库，并将消息发送给目标用户。
func (handler *MsgHandler) Handle(w http.ResponseWriter, r *http.Request) {
	ws, err := handler.upgrader.Upgrade(w, r, nil)
	defer func() {
		err := ws.Close()
		if err != nil {
			fmt.Printf("websocket close error %v \n", err)
		}
	}()
	if err != nil {
		fmt.Printf("WebSocket upgrade error %v \n", err)
		return
	}
	/*err = ws.SetReadDeadline(time.Now().Add(time.Millisecond * time.Duration(10000)))
	if err != nil {
		fmt.Printf("WebSocket SetReadDeadline() error %v",err)
	}*/
	// Subscribe

	var subMsg SubMsg
	msgType, bytes, err := ws.ReadMessage()
	if msgType != websocket.TextMessage {
		fmt.Print("websocket recv non-text msg \n")
		return
	}
	err = json.Unmarshal(bytes, &subMsg)
	if err != nil {
		fmt.Printf("websocket recv first msg error %v \n", err)
		return
	}

	check, syncTime, unsync := handler.checkUser(subMsg)
	if !check {
		return
	}
	var id uint64 = subMsg.Id
	if id == 0 {
		fmt.Println("websocket recv incorrect sub msg")
		return
	}
	channel := handler.channelMap.online(id)
	err = handler.syncMsg(ws, id, syncTime, unsync)
	if err != nil {
		return
	}
	go handler.listenSend(ws, channel)
	// Handle user send and transfer
	for {
		msgType, bytes, err := ws.ReadMessage()
		if err != nil {
			fmt.Printf("websocket recv error %v \n", err)
			break
		}
		if msgType == websocket.CloseMessage {
			fmt.Printf("client %v close the websocket \n", id)
			break
		} else if msgType != websocket.TextMessage {
			fmt.Printf("websocket recv non-text msg type : %v , content : %v \n", msgType, bytes)
			break
		}
		var msg *Msg = new(Msg)
		err = json.Unmarshal(bytes, msg)
		if err != nil {
			fmt.Printf("josn unmarshal error %v \n", err)
			break
		}
		err = SaveMsg(handler.db, msg)
		if err != nil {
			return
		}
		sendChannel, online := handler.channelMap.get(msg.To)
		if online {
			// 在线 直接向该协程发送消息
			sendChannel <- *msg
		} else {
			// 不在线
			handler.stockMap.save(msg.To, msg)
		}
	}
	channel <- CloseMsg()
	handler.channelMap.offline(id)
}

// checkUser 方法用于检查用户的身份验证，通过检查用户ID和密码是否匹配来验证用户身份。它会查询数据库以获取用户详细信息，并与传入的 SubMsg 结构进行比较。如果验证成功，将返回true，以及用户的同步时间和未同步消息数量。
func (handler *MsgHandler) checkUser(msg SubMsg) (bool, uint64, int) {
	// TODO check user by session
	user, err := sql.FetchUserDetails(handler.db, msg.Id)
	if err != nil {
		return false, 0, 0
	}
	return user.Pswd == msg.Pswd, user.SyncTime, user.UnSync
}

// syncMsg 方法用于同步离线消息，它会从内存中的消息存储中获取消息并发送给用户。如果内存中的消息不足以满足未同步消息的数量，它将从数据库中获取消息。

func (handler *MsgHandler) syncMsg(ws *websocket.Conn, id uint64, sync uint64, unsync int) error {
	// TODO db record msg sync
	msgList := handler.stockMap.getAndClear(id)
	if len(msgList) < unsync {
		fmt.Printf("user %v , unsync %v , memory contains %v , sync msg from database", id, unsync, len(msgList))
		msgList = FetchMsg(handler.db, id, sync)
	} else {
		fmt.Printf("user %v , unsync %v , sync msg from memory", id, unsync)
	}
	fmt.Printf(" num %v \n", len(msgList))
	for i := 0; i < len(msgList); i++ {
		bytes, err := json.Marshal(msgList[i])
		if err != nil {
			fmt.Printf("msg stock sync error %v \n", err)
			return err
		}
		err = ws.WriteMessage(websocket.TextMessage, bytes)
		if err != nil {
			fmt.Printf("msg stock sync error %v \n", err)
			return err
		}
	}
	var err error = nil
	if len(msgList) > 0 {
		err = MsgSyncRecord(handler.db, id, msgList[len(msgList)-1].Time)
	}
	return err
}

// listenSend 方法用于监听通道并将消息发送给WebSocket连接的客户端。
func (handler *MsgHandler) listenSend(ws *websocket.Conn, channel chan Msg) {
	for {
		msg := <-channel
		if msg.To == 0 {
			break
		}
		bytes, err := json.Marshal(msg)
		if err != nil {
			fmt.Printf("json stringify error %v \n", err)
			break
		}
		fmt.Printf("transfer msg %v \n", msg)
		err = ws.WriteMessage(websocket.TextMessage, bytes)
		if err != nil {
			fmt.Printf("json stringify error %v \n", err)
			break
		}
	}
}

// NewMsgHandler 函数用于创建一个新的 MsgHandler 实例，并初始化其中的字段。它接受一个数据库连接作为参数，并返回一个初始化后的 MsgHandler 结构体。

func NewMsgHandler(database *sqlx.DB) MsgHandler {
	return MsgHandler{
		upgrader:   websocket.Upgrader{},
		channelMap: NewChannelMap(),
		stockMap:   NewStockMap(),
		db:         database,
	}
}
