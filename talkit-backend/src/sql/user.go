package sql

import (
	"fmt"

	"github.com/jmoiron/sqlx"
)

type UserDetail struct {
	Id       uint64 `db:"id"`
	Name     string `db:"name"`
	Pswd     string `db:"pswd"`
	SyncTime uint64 `db:"sync"`
	UnSync   int    `db:"unsync"`
}
type Friend struct {
	Id   uint64 `json:"id" db:"id"`
	Name string `json:"name" db:"name"`
}
type User struct {
	Id   uint64 `json:"id" db:"id"`
	Name string `json:"name" db:"name"`
	Pswd string `json:"pswd" db:"pswd"`
}

func FetchUserDetails(db *sqlx.DB, id uint64) (*UserDetail, error) {
	var user UserDetail
	err := db.Get(&user, "select u.id as id,u.name as name,u.pswd as pswd,u.sync as sync,count(m.id) as unsync from tb_user u left join tb_msg m on u.id = m.target and m.time > u.sync where u.id = ? group by u.id", id)
	if err != nil {
		fmt.Println("select user detail error ", err)
		return nil, err
	}
	return &user, nil
}
func FetchUserInfoById(db *sqlx.DB, id uint64) (*Friend, error) {
	var user Friend
	err := db.Get(&user, "select id,name from tb_user where id = ?", id)
	if err != nil {
		fmt.Println("select user info error ", err)
		return nil, err
	}
	return &user, err
}
func FetchUserInfoByName(db *sqlx.DB, name string) (*User, error) {
	var user User
	err := db.Get(&user, "select id,name,pswd from tb_user where name = ?", name)
	if err != nil {
		fmt.Println("select user info error ", err)
		return nil, err
	}
	return &user, err
}
func FetchFriend(db *sqlx.DB, id uint64) ([]Friend, error) {
	var friends []Friend
	err := db.Select(&friends, "select f.friend_id as id, u.name as name from tb_friend f left join tb_user u on f.friend_id = u.id where f.user_id = ?", id)
	if err != nil {
		fmt.Println("select user friends error ", err)
		return nil, err
	}
	return friends, err
}
func FindUser(db *sqlx.DB, id uint64) ([]Friend, error) {
	var friends []Friend
	err := db.Select(&friends, "select id,  name from tb_user where id = ?", id)
	if err != nil {
		fmt.Println("select user friends error ", err)
		return nil, err
	}
	return friends, err
}

func AddFriend(db *sqlx.DB, id uint64, friendId uint64) error {
	_, err := db.Exec("insert into tb_friend values (?,?),(?,?)", id, friendId, friendId, id)
	if err != nil {
		fmt.Println("AddFriend error ", err)
	}
	return err
}
func InsertUser(db *sqlx.DB, name string, pswd string) error {
	_, err := db.Exec("insert into tb_user (name,pswd,sync) values ( ? , ? , 0)", name, pswd)
	if err != nil {
		fmt.Println("InsertUser error ", err)
	}
	return err
}
