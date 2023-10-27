package main

import (
	"fmt"
	"net"
	"net/http"
	"talkit-backend/src/req"
	"talkit-backend/src/router"
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
	go func() {
		fmt.Println("Server start, port 3000")
		err := http.ListenAndServe(":3000", nil)
		if err != nil {
			fmt.Printf("HTTP server listen and serve error %v", err)
		}
	}()
	// fmt.Println("server start, port 3000")
	// err = http.ListenAndServe(":3000", nil)
	// if err != nil {
	// 	fmt.Printf("http server listen and serve error %v", err)
	// }
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		fmt.Println(err)
		fmt.Println("video")
		return
	}

	for _, address := range addrs {
		if ipnet, ok := address.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				fmt.Printf("Listen on: %s:%d\n", ipnet.IP.String(), 8000)
			}
		}
	}

	// Start the HTTP server for port 8000 in a goroutine
	go func() {
		router.Run()
	}()
	// fmt.Println("server start, port 8000")
	//router.Run(8000)
	// Prevent the main function from exiting immediately
	select {}
}
