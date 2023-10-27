package sdx.talk.entity;

public class RoomCommandEntity {

    private final String userId;
    private final String roomName;

    public RoomCommandEntity(String userId, String roomName) {
        this.userId = userId;
        this.roomName = roomName;
    }

    public String getUserId() {
        return userId;
    }

    public String getRoomName() {
        return roomName;
    }
}
