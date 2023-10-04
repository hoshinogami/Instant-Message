package req

import (
	"encoding/json"
	"fmt"
	"strconv"
)

type Result struct {
	code byte
	msg  string
	data []byte
}

func (result Result) ToJSON() []byte {
	resStr := "{code:" + strconv.Itoa(int(result.code)) + ",msg:\"" + result.msg + "\",data:"
	if result.data == nil || len(result.data) == 0 {
		resStr += "\"\"}"
	} else {
		resStr += string(result.data)
		resStr += "}"
	}
	return []byte(resStr)
}

// Success 函数用于创建成功的结果。
func Success(data interface{}) []byte {
	marshal, err := json.Marshal(data)
	if err != nil {
		fmt.Println("result.Success() json.Marshal error ", err)
	}
	return Result{
		code: 0,
		msg:  "",
		data: marshal,
	}.ToJSON()
}

// Failed 函数用于创建失败的结果。
func Failed(code byte, msg string) []byte {
	return Result{
		code: code,
		msg:  msg,
		data: nil,
	}.ToJSON()
}
