package server_command;

import server.ChatManager;
import server.ServerConnection;

public class CreateRoomCommand extends ServerCommand{
    private String roomid;
    private final String type = "createroom";

    public CreateRoomCommand(String roomid){
        this.roomid = roomid;
    }

    @Override
    public void execute(ServerConnection serverConnection) {
        ChatManager chatManager = serverConnection.getChatManager();
        String jsonMessage;
        if (chatManager.createRoom(serverConnection, this.roomid) ){
            jsonMessage = ListCommand.buildRoomList(chatManager,null, null);
        }else{
            jsonMessage = ListCommand.buildRoomList(chatManager,this.roomid , null);
        }
        System.out.println("Send: " + jsonMessage);
        chatManager.sendToOneClient(jsonMessage, serverConnection);

    }
}
