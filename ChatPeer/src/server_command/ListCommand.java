package server_command;

import client_command.RoomListCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;

public class ListCommand extends ServerCommand{
    private final String type = "list";

    @Override
    public void execute(IConnection connection) {
        System.out.println("receive list request from " + connection.getName());
        ChatManager chatManager = connection.getChatManager();
        String roomList = buildRoomList(chatManager, null, null);
//        System.out.println("Send: " + roomList);
        chatManager.sendToOneClient(roomList, connection);

    }

    public static String buildRoomList(ChatManager chatManager, String ignore, String addition){
        Gson gson = new Gson();

        RoomListCommand roomListCommand = new RoomListCommand(chatManager.getRoomsInfo(ignore, addition));
        return gson.toJson(roomListCommand);
    }
}
