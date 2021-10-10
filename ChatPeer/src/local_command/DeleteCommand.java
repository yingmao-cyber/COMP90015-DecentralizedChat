package local_command;

import client.ChatClient;
import client_command.RoomChangeCommand;
import client_command.RoomChangeOnDelete;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;
import server_command.JoinCommand;
import server_command.ListCommand;
import server_command.ServerCommand;

import java.util.ArrayList;

public class DeleteCommand extends LocalCommand {
    private String roomid;
    private final String type = "delete";

    public DeleteCommand(String roomid){
        this.roomid = roomid;
    }

    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager) {
        Gson gson = new Gson();
        if (!chatManager.isRoomIdExist(roomid)){
            System.out.println(roomid + " does not exist.");
        }
        ArrayList<ServerConnection> clientsInRoom = chatManager.getChatRooms(roomid);
        ArrayList<String> deleted = new ArrayList<>();
        for (ServerConnection client: clientsInRoom){
            deleted.add(client.getName());
        }

        /** treat as clients join the empty room */
        for (ServerConnection client: clientsInRoom){
            RoomChangeOnDelete roomChangeOnDelete = new RoomChangeOnDelete(deleted);
            String jsonMessage = gson.toJson(roomChangeOnDelete);
            chatManager.sendToOneClient(jsonMessage, client);
            client.setCurrentChatRoom("");
        }
        chatManager.removeRoom(roomid);
    }
}
