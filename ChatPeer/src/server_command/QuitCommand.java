package server_command;

import client_command.RoomChangeCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;

public class QuitCommand extends ServerCommand{
    private final String type = "quit";

    @Override
    public void execute(ServerConnection serverConnection){
        Gson gson = new Gson();
        ChatManager chatManager = serverConnection.getChatManager();

        /** send RoomChange event to the disconnecting client */
        String roomid = "";
        RoomChangeCommand roomChangeCommand = new RoomChangeCommand(
                serverConnection.getName(), serverConnection.getCurrentChatRoom(), roomid);
        String jsonMessage = gson.toJson(roomChangeCommand);
        System.out.println("Send: " + jsonMessage);

        chatManager.broadCastToCurrentRoom(serverConnection, jsonMessage, null);

        chatManager.removeClientConnection(serverConnection);
    }
}
