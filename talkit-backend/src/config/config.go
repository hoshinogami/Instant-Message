package config

import (
	"encoding/json"
)

func ObjectToJSON(obj interface{}) (string, error) {
	data, err := json.Marshal(obj)
	if err != nil {
		return "", err
	}
	return string(data), nil
}

func JSONToObject(jsonString string, obj interface{}) error {
	err := json.Unmarshal([]byte(jsonString), obj)
	if err != nil {
		return err
	}
	return nil
}
