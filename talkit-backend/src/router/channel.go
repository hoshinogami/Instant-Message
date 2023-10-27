package router

import (
	"sync"
)

// ChannelMap 结构体表示一个通道（channel）的映射表，它有两个字段：

// innerMap：一个映射（map）数据结构，用于存储uint64类型的键（表示通道的ID）和 chan Msg 类型的值（表示通道本身）。
// mutex：一个读写互斥锁（sync.RWMutex）用于对 innerMap 进行并发读写控制。
type ChannelMap struct {
	innerMap map[string]chan mes
	mutex    sync.RWMutex
}

// get 方法用于获取指定ID的通道。它接受一个 id 参数，该参数是要获取的通道的ID。首先，它通过调用 RLock() 方法对 mutex 进行读锁定，以允许多个goroutine同时读取 innerMap。然后，它尝试从 innerMap 中查找指定ID的通道，并返回该通道和一个布尔值，表示是否成功找到通道。最后，它通过调用 RUnlock() 方法释放读锁。
func (channelMap *ChannelMap) get(id string) (chan mes, bool) {
	channelMap.mutex.RLock()
	channel, ok := channelMap.innerMap[id]
	channelMap.mutex.RUnlock()
	return channel, ok
}

// offline 方法用于从映射表中删除指定ID的通道。它接受一个 id 参数，该参数是要删除的通道的ID。类似于 online 方法，它通过调用 Lock() 方法对 mutex 进行写锁定，以确保安全地删除通道。然后，它使用 delete() 函数从 innerMap 中删除指定ID的通道。最后，它释放写锁定。
func (channelMap *ChannelMap) online(id string) chan mes {
	channel := make(chan mes, 32)
	channelMap.mutex.Lock()
	channelMap.innerMap[id] = channel
	channelMap.mutex.Unlock()
	return channel
}

// offline 方法用于从映射表中删除指定ID的通道。它接受一个 id 参数，该参数是要删除的通道的ID。类似于 online 方法，它通过调用 Lock() 方法对 mutex 进行写锁定，以确保安全地删除通道。然后，它使用 delete() 函数从 innerMap 中删除指定ID的通道。最后，它释放写锁定。
func (channelMap *ChannelMap) offline(id string) {
	channelMap.mutex.Lock()
	delete(channelMap.innerMap, id)
	channelMap.mutex.Unlock()
}

// NewChannelMap 函数用于创建一个新的 ChannelMap 实例。它返回一个初始化了 innerMap 和 mutex 字段的 ChannelMap 结构体。
func NewChannelMap() ChannelMap {
	return ChannelMap{
		innerMap: make(map[string]chan mes, 64),
		mutex:    sync.RWMutex{},
	}
}
