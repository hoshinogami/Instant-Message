package ws

import (
	"fmt"
	"sync"
)

type MsgStock struct {
	msgList []Msg
}

type MsgStockMap struct {
	mutex    sync.RWMutex
	innerMap map[uint64]*MsgStock
}

// save 方法用于将一条消息保存到 MsgStock 结构体中。如果 MsgStock 中已经有消息，则将新消息追加到 msgList 切片中。如果 MsgStock 为空，将创建一个新的 msgList 切片并将消息存储在其中。
func (stock *MsgStock) save(msg *Msg) {
	if len(stock.msgList) == 0 {
		stock.msgList = []Msg{*msg}
	} else {
		stock.msgList = append(stock.msgList, *msg)
	}
}

// save 方法用于将一条消息保存到特定用户的消息存储中。它接受一个用户ID (id uint64) 和一条消息 (msg *Msg) 作为参数。首先，它通过读锁定 (RLock()) 查找指定ID的 MsgStock。如果找到了，它将调用 save 方法将消息保存到 MsgStock 中。如果没有找到，它将释放读锁，然后获取写锁定 (Lock()) 来创建一个新的 MsgStock 并将消息存储在其中。
func (msgStockMap *MsgStockMap) save(id uint64, msg *Msg) {
	msgStockMap.mutex.RLock()
	stock, ok := msgStockMap.innerMap[id]
	msgStockMap.mutex.RUnlock()
	if ok {
		stock.save(msg)
	} else {
		msgStockMap.mutex.Lock()
		// 编译器逃逸分析 会在堆上分配
		msgStockMap.innerMap[id] = &MsgStock{
			msgList: []Msg{*msg},
		}
		msgStockMap.mutex.Unlock()
	}
}

// getAndClear 方法用于获取指定用户的未发送消息并清除该用户的消息存储。它首先通过读锁定查找指定ID的 MsgStock，然后返回其中的消息列表。如果 MsgStock 为空，返回一个空的消息列表，并将 MsgStock 中的消息清空（置为 nil）。
func (msgStockMap *MsgStockMap) getAndClear(id uint64) []Msg {
	msgStockMap.mutex.RLock()
	stock := msgStockMap.innerMap[id]
	msgStockMap.mutex.RUnlock()
	if stock == nil || len(stock.msgList) == 0 {
		return []Msg{}
	} else {
		list := stock.msgList
		stock.msgList = nil
		fmt.Println(stock.msgList)
		return list
	}
}

// NewStockMap 函数用于创建一个新的 MsgStockMap 实例，并初始化其中的字段。它返回一个初始化后的 MsgStockMap 结构体。
func NewStockMap() MsgStockMap {
	return MsgStockMap{
		innerMap: make(map[uint64]*MsgStock, 64),
		mutex:    sync.RWMutex{},
	}
}
