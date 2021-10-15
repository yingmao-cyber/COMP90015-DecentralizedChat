package server_command;

import client_command.RoomChangeCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;

public class QuitCommand extends ServerCommand{
    private final String type = "quit";

    @Override
    public void execute(IConnection connection){
        Gson gson = new Gson();
        ChatManager chatManager = connection.getChatManager();

        /** send RoomChange event to the disconnecting client */
        String roomid = "";
        RoomChangeCommand roomChangeCommand = new RoomChangeCommand(
                connection.getName(), connection.getCurrentChatRoom(), roomid);
        String jsonMessage = gson.toJson(roomChangeCommand);

        chatManager.broadCastToCurrentRoom(connection, jsonMessage, null);

        chatManager.removeClientConnection(connection);
    }
}
