package session

import (
	"sync"

	"github.com/gorilla/websocket"
)

type SessionManager struct {
	sync.Mutex
	Sessions map[string]*websocket.Conn
}

func NewSessionManager() *SessionManager {
	return &SessionManager{
		Sessions: make(map[string]*websocket.Conn),
	}
}

func (sm *SessionManager) AddSession(userId string, conn *websocket.Conn) {
	sm.Lock()
	sm.Sessions[userId] = conn
	sm.Unlock()
}

func (sm *SessionManager) GetSession(userId string) *websocket.Conn {
	sm.Lock()
	conn, ok := sm.Sessions[userId]
	sm.Unlock()
	if ok {
		return conn
	}
	return nil
}

func (sm *SessionManager) RemoveSession(userId string) {
	sm.Lock()
	conn, ok := sm.Sessions[userId]
	if ok {
		delete(sm.Sessions, userId)
		conn.Close()
	}
	sm.Unlock()
}

func (sm *SessionManager) RemoveSessionByConnection(conn *websocket.Conn) {
	sm.Lock()
	for userId, c := range sm.Sessions {
		if c == conn {
			delete(sm.Sessions, userId)
			conn.Close()
			break
		}
	}
	sm.Unlock()
}

func (sm *SessionManager) GetSessionSize() int {
	sm.Lock()
	size := len(sm.Sessions)
	sm.Unlock()
	return size
}

func (sm *SessionManager) GetUserIds() []string {
	sm.Lock()
	userIds := make([]string, 0, len(sm.Sessions))
	for userId := range sm.Sessions {
		userIds = append(userIds, userId)
	}
	sm.Unlock()
	return userIds
}

func (sm *SessionManager) GetSessions() []*websocket.Conn {
	sm.Lock()
	sessions := make([]*websocket.Conn, 0, len(sm.Sessions))
	for _, conn := range sm.Sessions {
		sessions = append(sessions, conn)
	}
	sm.Unlock()
	return sessions
}
