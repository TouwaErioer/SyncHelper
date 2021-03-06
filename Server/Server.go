package main

import (
	"encoding/json"
	"fmt"
	"log"
	"net"
	"os"
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
	var (
		agree string
	)
	fmt.Printf("Agree %s connect this computer? (Enter agree, enter any reject) ", c.RemoteAddr())
	fmt.Scanln(&agree)
	if len(agree) == 0 {
		c.Write([]byte("agree"))
		fmt.Println("Connection successful")
	Listener:
		for {
			temp, err := c.Read(buf)
			if err != nil {
				c.Close()
				break
			}
			var data Notify
			str := strings.TrimSpace(string(buf[0:temp]))
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
			case "File":
				file, err := os.Create(data.Title)
				if err != nil {
					fmt.Printf("create file error:%s\n", err)
					break
				}
				defer file.Close()
				for {
					cnt, err := c.Read(buf)
					if err != nil || cnt == 0 {
						c.Close()
						goto Listener
					}
					file.Write(buf[:cnt])
				}
			}
		}
	} else {
		c.Write([]byte("reject"))
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
