package bean

import (
	"encoding/json"
	"fmt"
)

type LoginRequsetBean struct {
	Code       int
	SequenceId string
	UserId     string
}

type LoginResponseBean struct {
	Code       int
	SequenceId string
	IsSucceed  bool
	Message    string
}

type SendMsgRequestBean struct {
	Code       int
	SequenceId string
	FromUserId string
	ToUserId   string
	Message    string
}

type SendMsgResponseBean struct {
	Code       int
	SequenceId string
	FromUserId string
	ToUserId   string
	IsSucceed  bool
	Message    string
}

type OffLineResponseBean struct {
	Code       int
	SequenceId string
	Message    string
}

type OnLineUserInfoResponseBean struct {
	Code       int
	SequenceId string
	UserIdList []string
}

func bean() {
	// 在Go中，你可以使用这些结构体来创建和处理数据
	loginRequest := LoginRequsetBean{
		Code:       1,
		SequenceId: "123",
		UserId:     "user123",
	}

	// 将结构体转为JSON字符串
	loginRequestJSON, err := json.Marshal(loginRequest)
	if err != nil {
		fmt.Println("JSON marshaling error:", err)
	} else {
		fmt.Println(string(loginRequestJSON))
	}

	// 反向操作：将JSON字符串转为结构体
	var receivedLoginRequest LoginRequsetBean
	err = json.Unmarshal([]byte(`{"Code":1,"SequenceId":"123","UserId":"user123"}`), &receivedLoginRequest)
	if err != nil {
		fmt.Println("JSON unmarshaling error:", err)
	} else {
		fmt.Printf("%+v\n", receivedLoginRequest)
	}
}
