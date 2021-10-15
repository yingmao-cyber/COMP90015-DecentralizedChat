package local_command;

import client.ChatClient;
import client_command.RoomChangeCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;

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
        ArrayList<IConnection> clientsInRoom = chatManager.getChatRooms(roomid);
        /** treat as clients join the empty room */
        for (IConnection client: clientsInRoom){
            RoomChangeCommand roomChangeCommand = new RoomChangeCommand(chatClient.getIdentity(),
                    chatClient.getRoomid(), "d");
            String jsonMessage = gson.toJson(roomChangeCommand);
            chatManager.sendToOneClient(jsonMessage, client);
            client.setCurrentChatRoom("");
        }
        chatManager.removeRoom(roomid);
        if (!chatClient.isConnectedLocally()){
            chatClient.printPrefix();
        }
    }
}
