package server_command;

import client_command.RoomContentsCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;

import java.util.ArrayList;

public class WhoCommand extends ServerCommand{
    private String roomid;
    private final String type = "who";

    public WhoCommand(String roomid){
        this.roomid = roomid;
    }

    @Override
    public void execute(ServerConnection serverConnection) {

        ChatManager chatManager = serverConnection.getChatManager();
        chatManager.removeEmptyRoomWithOwnerDropped();
        String jsonMessage = buildRoomContent(chatManager, roomid);
        System.out.println("Send: " + jsonMessage);
        chatManager.sendToOneClient(jsonMessage, serverConnection);
    }


    public static String buildRoomContent(ChatManager chatManager, String roomid){
        Gson gson = new Gson();
        String owner = chatManager.getRoomOwner(roomid);
        /** when room owner gets disconnected, owner is set to an empty string */
        if (owner == null){
            owner = "";
        }
        ArrayList<String> identities = chatManager.getRoomIdentities(roomid);
        RoomContentsCommand roomContentsCommand = new RoomContentsCommand(roomid, identities, owner);
        return gson.toJson(roomContentsCommand);
    }

}
