package local_command;

import client.ChatClient;
import client_command.RoomChangeCommand;
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
        ArrayList<ServerConnection> clientsInRoom = chatManager.getChatRooms(roomid);
        /** treat as clients join the empty room */
        for (ServerConnection client: clientsInRoom){
            RoomChangeCommand roomChangeCommand = new RoomChangeCommand(
                    client.getName(), client.getCurrentChatRoom(), "");
            String jsonMessage = gson.toJson(roomChangeCommand);
            chatManager.sendToOneClient(jsonMessage, client);
            client.setCurrentChatRoom("");
        }
        chatManager.removeRoom(roomid);
    }
}
