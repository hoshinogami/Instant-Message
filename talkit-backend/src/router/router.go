package router

import (
	"encoding/json"
	"fmt"
	"net/http"

	"github.com/gorilla/websocket"
)

type mes struct {
	Code       int    `json:"code"`
	FromUserId string `json:"fromUserId"`
	Message    string `json:"message"`
	SequenceId string `json:"sequenceId"`
	ToUserId   string `json:"toUserId"`
}
type loginMes struct {
	Code       int    `json:"code"`
	UserId     string `json:"userId"`
	SequenceId string `json:"sequenceId"`
}
type MsgHandler struct {
	upgrader   websocket.Upgrader
	channelMap ChannelMap
}

const (
	LoginCode          = 0 // 客户端登录code
	SendMsgCode        = 1 // 服务端帮忙转发消息code
	OFFLineCode        = 2 // 服务端主动断线code
	OnLineUserInfoCode = 4 // 服务端主动推送在线用户信息code
)

var handler MsgHandler = NewMsgHandler()

// WebSocketHandler 处理WebSocket连接和消息
func (handler *MsgHandler) WebSocketHandler(w http.ResponseWriter, r *http.Request) {
	conn, err := handler.upgrader.Upgrade(w, r, nil)
	if err != nil {
		fmt.Println("Error upgrading to WebSocket:", err)
		return
	}
	defer conn.Close()
	var subMsg loginMes
	//for {
	// 从WebSocket连接接收消息
	_, bytes, err := conn.ReadMessage()
	err = json.Unmarshal(bytes, &subMsg)
	if err != nil {
		fmt.Println("Error reading message:", err)
		return
	}
	fmt.Printf("数据 %v\n", bytes)
	// 在这里处理接收到的消息

	// 使用 strconv.ParseUint 函数来将字符串转换为 uint64
	// if id == 0 {
	// 	fmt.Println("websocket recv incorrect sub msg")
	// 	return
	// } else {
	// 	fmt.Printf("id %v\n", subMsg.UserId)
	// }
	channel := handler.channelMap.online(subMsg.UserId)
	fmt.Printf("上线成功 %v\n", subMsg.UserId)
	go handler.listenSend(conn, channel)
	for {
		msgType, bytes, err := conn.ReadMessage()
		if err != nil {
			fmt.Printf("websocket recv error %v \n", err)
			break
		}
		if msgType == websocket.CloseMessage {
			fmt.Printf("client %v close the websocket \n", subMsg)
			break
		} else if msgType != websocket.TextMessage {
			fmt.Printf("websocket recv non-text msg type : %v , content : %v \n", msgType, bytes)
			break
		}
		var msg *mes = new(mes)
		err = json.Unmarshal(bytes, msg)
		if err != nil {
			fmt.Printf("josn unmarshal error %v \n", err)
			break
		}
		if err != nil {
			return
		}

		sendChannel, online := handler.channelMap.get(msg.ToUserId)
		if online {
			// 在线 直接向该协程发送消息
			sendChannel <- *msg
		} else {
			// 不在线
			return
		}
	}
	//handleSendMsg(subMsg, conn)

	channel <- CloseMsg()
	handler.channelMap.offline(subMsg.UserId)

	//}
}
func CloseMsg() mes {
	return mes{
		ToUserId: "0",
	}
}
func Run() {
	http.HandleFunc("/ws2", func(writer http.ResponseWriter, req *http.Request) {
		handler.WebSocketHandler(writer, req)
	})

	// 启动WebSocket服务器
	err := http.ListenAndServe(":8080", nil)
	if err != nil {
		fmt.Println("Error starting WebSocket server:", err)
	}
}

// listenSend 方法用于监听通道并将消息发送给WebSocket连接的客户端。
func (handler *MsgHandler) listenSend(ws *websocket.Conn, channel chan mes) {
	for {
		msg := <-channel
		if msg.ToUserId == "0" {
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
func NewMsgHandler() MsgHandler {
	return MsgHandler{
		upgrader:   websocket.Upgrader{},
		channelMap: NewChannelMap(),
	}
}
