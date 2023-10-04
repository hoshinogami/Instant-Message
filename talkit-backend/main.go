package main

import (
	"fmt"
	"net/http"
	"talkit-backend/src/req"
	"talkit-backend/src/ws"

	_ "github.com/go-sql-driver/mysql"
	"github.com/jmoiron/sqlx"
)

func main() {
	db, err := sqlx.Open("mysql", "hoshi:123456@tcp(127.0.0.1:3306)/talkit")
	defer func() {
		err = db.Close()
		if err != nil {
			fmt.Println("db close error ", err)
		}
	}()
	if err != nil {
		fmt.Println("connect mysql error ", err)
	}
	var msgHandler ws.MsgHandler = ws.NewMsgHandler(db)
	http.HandleFunc("/ws", func(writer http.ResponseWriter, req *http.Request) {
		msgHandler.Handle(writer, req)
	})
	httpHandler := req.NewHttpHandler(db)
	httpHandler.Mount()
	fmt.Println("server start, port 3000")
	err = http.ListenAndServe(":3000", nil)
	if err != nil {
		fmt.Printf("http server listen and serve error %v", err)
	}
}
