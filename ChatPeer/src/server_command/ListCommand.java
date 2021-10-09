package server_command;

import client_command.RoomChangeCommand;
import client_command.RoomListCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;

import java.util.HashMap;

public class ListCommand extends ServerCommand{
    private final String type = "list";

    @Override
    public void execute(ServerConnection serverConnection) {
        ChatManager chatManager = serverConnection.getChatManager();
        String roomList = buildRoomList(chatManager, null, null);
        System.out.println("Send: " + roomList);
        chatManager.sendToOneClient(roomList, serverConnection);

    }

    public static String buildRoomList(ChatManager chatManager, String ignore, String addition){
        Gson gson = new Gson();
        chatManager.removeEmptyRoomWithOwnerDropped();

        RoomListCommand roomListCommand = new RoomListCommand(chatManager.getRoomsInfo(ignore, addition));
        return gson.toJson(roomListCommand);
    }
}
