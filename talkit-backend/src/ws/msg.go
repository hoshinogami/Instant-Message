package ws

import (
	"fmt"

	"github.com/jmoiron/sqlx"
)

type Msg struct {
	From uint64 `json:"from" db:"src"`
	To   uint64 `json:"to" db:"target"`
	Text string `json:"text" db:"text"`
	Time uint64 `json:"time" db:"time"`
	Type uint64 `json:"type" db:"type"`
}

// CloseMsg 函数返回一个特殊的 Msg 结构体，用于表示关闭连接的消息。它将 To 字段设置为0，通常用于在WebSocket连接关闭时发送给客户端。
func CloseMsg() Msg {
	return Msg{
		To: 0,
	}
}

type SubMsg struct {
	Id   uint64 `json:"id"`
	Pswd string `json:"pswd"`
}

// SaveMsg 函数用于将一条消息保存到数据库中。它接受一个数据库连接 (*sqlx.DB) 和一个消息结构体 (msg *Msg) 作为参数，并将消息的内容插入到数据库表中。如果保存过程中出现错误，函数会打印错误信息并返回错误。
func SaveMsg(db *sqlx.DB, msg *Msg) error {
	_, err := db.Exec("insert into tb_msg (src,target,time,text,type) values (?,?,?,?,?)", msg.From, msg.To, msg.Time, msg.Text, msg.Type)
	if err != nil {
		fmt.Println("save msg error ", err)
		return err
	}
	return nil
}

// FetchMsg 函数s用于从数据库中检索指定用户的未同步消息。它接受一个数据库连接 (*sqlx.DB)、用户ID (id uint64) 和同步时间戳 (sync uint64) 作为参数，并返回一个消息结构体的切片，表示从数据库中检索到的消息列表。
func FetchMsg(db *sqlx.DB, id uint64, sync uint64) []Msg {
	var msgList []Msg
	err := db.Select(&msgList, "select src,target,time,text,type from tb_msg where target = ? and time > ?", id, sync)
	if err != nil {
		fmt.Println("msg fetch error")
		return nil
	}
	return msgList
}

// MsgSyncRecord 函数用于更新用户的同步时间戳。它接受一个数据库连接 (*sqlx.DB)、用户ID (id uint64) 和同步时间戳 (sync uint64) 作为参数，并更新数据库中的用户记录以反映最新的同步时间。如果更新过程中出现错误，函数会打印错误信息并返回错误。
func MsgSyncRecord(db *sqlx.DB, id uint64, sync uint64) error {
	_, err := db.Exec("update tb_user set sync = ? where id = ?", sync, id)
	if err != nil {
		fmt.Println("msg sync record change error ", err)
		return err
	}
	return nil
}
