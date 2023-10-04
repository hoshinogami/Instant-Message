package req

import (
	"crypto/md5"
	"fmt"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"talkit-backend/src/sql"

	"github.com/jmoiron/sqlx"
)

// 加密
func Md5New(pwd string) (string, error) {
	m := md5.New()
	_, err := io.WriteString(m, pwd)
	if err != nil {
		return "", err
	}
	arr := m.Sum(nil)
	return fmt.Sprintf("%x", arr), nil
}

type HttpHandler struct {
	db *sqlx.DB
}

func NewHttpHandler(db *sqlx.DB) HttpHandler {
	return HttpHandler{
		db: db,
	}
}

func (handler HttpHandler) Mount() {
	http.HandleFunc("/hello", func(writer http.ResponseWriter, req *http.Request) {
		_, err := writer.Write([]byte("hello,world"))
		if err != nil {
			fmt.Printf("%v", err)
		}
	})
	http.HandleFunc("/login", func(writer http.ResponseWriter, req *http.Request) {
		params := req.URL.Query()
		name := params.Get("name")
		user, err := sql.FetchUserInfoByName(handler.db, name)
		if err != nil {
			return
		}
		if params.Get("pswd") != user.Pswd {
			_, err := writer.Write(Failed(1, ""))
			if err != nil {
				fmt.Println("/login http write error", err)
			}
			return
		}
		userInfo := sql.Friend{
			Id:   user.Id,
			Name: user.Name,
		}
		_, err = writer.Write(Success(userInfo))
		if err != nil {
			fmt.Println("/login http write error", err)
		}
	})
	http.HandleFunc("/register", func(writer http.ResponseWriter, req *http.Request) {
		params := req.URL.Query()
		name := params.Get("name")
		pswd := params.Get("pswd")
		fmt.Println("name", name, "pswd", pswd)
		err := sql.InsertUser(handler.db, name, pswd)
		if err != nil {
			if strings.Contains(err.Error(), "Duplicate") {
				_, err = writer.Write(Failed(1, "用户名重复"))
			} else {
				_, err = writer.Write(Failed(1, "未知原因"))
			}
		} else {
			_, err = writer.Write(Success(nil))
		}
		if err != nil {
			fmt.Println("http /register write error ", err)
		}
	})
	http.HandleFunc("/friend/get", func(writer http.ResponseWriter, req *http.Request) {
		params := req.URL.Query()
		id, err := strconv.Atoi(params.Get("id"))
		if err != nil {
			fmt.Println("/friend/get http param err ", err)
			return
		}
		friends, err := sql.FetchFriend(handler.db, uint64(id))
		_, err = writer.Write(Success(friends))
		if err != nil {
			fmt.Println("/friend/get write error ", err)
		}
	})
	http.HandleFunc("/friend/add", func(writer http.ResponseWriter, req *http.Request) {
		params := req.URL.Query()

		id, err := strconv.Atoi(params.Get("id"))
		if err != nil {
			fmt.Println("/friend/add http param err ", err)
			return
		}
		friendId, err := strconv.Atoi(params.Get("friendId"))
		if err != nil {
			fmt.Println("/friend/add http param err ", err)
			return
		}
		err = sql.AddFriend(handler.db, uint64(id), uint64(friendId))
		if err != nil {
			_, err = writer.Write(Failed(1, ""))
		} else {
			_, err = writer.Write(Success(nil))
		}
		if err != nil {
			fmt.Println("/friend/get write error ", err)
		}
	})
	http.HandleFunc("/friend/find", func(writer http.ResponseWriter, req *http.Request) {
		params := req.URL.Query()

		friendId, err := strconv.Atoi(params.Get("friendId"))
		if err != nil {
			fmt.Println("/friend/find http param err ", err)
			return
		}
		friends, err := sql.FindUser(handler.db, uint64(friendId))
		if err != nil {
			_, err = writer.Write(Failed(1, ""))
		} else {
			_, err = writer.Write(Success(friends))
		}
		if err != nil {
			fmt.Println("/friend/get write error ", err)
		}
	})
	http.HandleFunc("/image/upload", func(w http.ResponseWriter, r *http.Request) {
		// 解析请求中的图片文件
		r.ParseMultipartForm(10 << 20) // 设置最大文件大小

		file, handler, err := r.FormFile("image") // 表单字段的名称为 "image"
		if err != nil {
			http.Error(w, "Unable to read file", http.StatusBadRequest)
			return
		}
		defer file.Close()

		// 在服务器上保存上传的图片
		// 请替换 "uploads" 为您存储图片的目录
		os.MkdirAll("image/", os.ModePerm)
		imageFilePath := filepath.Join("image/", handler.Filename)
		outFile, err := os.Create(imageFilePath)
		if err != nil {
			http.Error(w, "Unable to save file", http.StatusInternalServerError)
			return
		}
		defer outFile.Close()

		_, err = io.Copy(outFile, file)
		if err != nil {
			http.Error(w, "Unable to save file", http.StatusInternalServerError)
			return
		}

		// 返回成功响应
		w.WriteHeader(http.StatusCreated)
		w.Write([]byte("Image uploaded successfully"))
	})
	http.HandleFunc("/image/download", func(w http.ResponseWriter, r *http.Request) {
		// 	vars := mux.Vars(r)
		// 	filename := vars["filename"] // 请求中的文件名

		// 	// 从服务器上读取图片并发送给客户端
		// 	// 请替换 "uploads" 为您存储图片的目录
		// 	imageFilePath := filepath.Join("/image/", filename)
		// 	imageFile, err := os.Open(imageFilePath)
		// 	fmt.Print(imageFilePath)
		// 	if err != nil {
		// 		fmt.Println(err)
		// 		http.Error(w, "Image not found", http.StatusNotFound)
		// 		return
		// 	}
		// 	defer imageFile.Close()

		// 	// 设置响应的 Content-Type 为图片类型
		// 	contentType := http.DetectContentType([]byte(filename))
		// 	w.Header().Set("Content-Type", contentType)

		// 	// 将图片内容发送给客户端
		// 	_, err = io.Copy(w, imageFile)
		// 	if err != nil {
		// 		fmt.Println(err)
		// 		http.Error(w, "Unable to send image", http.StatusInternalServerError)
		// 		return
		// 	}
		// 获取URL中的参数(Get请求)
		queryParams := r.URL.Query()

		// 获取特定的值（拿到需要下载的文件名称）
		filePath := queryParams.Get("filename")

		if filePath == "" {
			// 直接返回响应
			fmt.Fprintf(w, "%s", "文件名不能为空！")
			return
		}

		// 和 OS 交互
		file, err := os.Open("image/" + filePath)
		if err != nil {
			fmt.Println(err)
			fmt.Fprintf(w, "Sorry：%s", "找不到对应的文件！")
			return
		}
		defer file.Close()

		// 设置响应头，告诉浏览器该文件需要下载而不是直接展示
		w.Header().Set("Content-Disposition", "attachment; filename="+filePath)

		// 将文件内容写入响应体
		_, err = io.Copy(w, file)
		if err != nil {
			fmt.Println(err)
			return
		}
	})
}
