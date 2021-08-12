package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"strings"

	"github.com/atotto/clipboard"
	"github.com/go-toast/toast"
)

type Notify struct {
	Type    string
	Title   string
	Content string
}

func connHandler(c net.Conn) {
	if c == nil {
		return
	}

	buf := make([]byte, 4096)

	for {
		cnt, err := c.Read(buf)
		if err != nil || cnt == 0 {
			c.Close()
			break
		}

		str := strings.TrimSpace(string(buf[0:cnt]))

		fmt.Println(c.RemoteAddr())
		var data Notify
		json.Unmarshal([]byte(str), &data)
		switch data.Type {
		case "Notification":
			notification := toast.Notification{
				AppID:   "Microsoft.Windows.Shell.RunDialog",
				Title:   data.Title,
				Message: data.Content,
			}
			err := notification.Push()
			if err != nil {
				log.Fatalln(err)
			}
		case "Clipboard":
			clipboard.WriteAll(data.Content)
		default:
			fmt.Print(str)
		}
	}

	fmt.Printf("Connection from %v closed. \n", c.RemoteAddr())
}

func main() {
	server, err := net.Listen("tcp", ":1208")
	if err != nil {
		fmt.Printf("Fail to start server, %s\n", err)
	}

	fmt.Println("Server Started ...")

	for {
		conn, err := server.Accept()
		if err != nil {
			fmt.Printf("Fail to connect, %s\n", err)
			break
		}

		go connHandler(conn)
	}
}
