package server_command;

import server.ChatManager;
import server.ServerConnection;

public class DeleteCommand extends ServerCommand{
    private String roomid;
    private final String type = "delete";

    public DeleteCommand(String roomid){
        this.roomid = roomid;
    }

    @Override
    public void execute(ServerConnection serverConnection) {
        ChatManager chatManager = serverConnection.getChatManager();
        String jsonMessage;
        if (chatManager.deleteRoom(serverConnection, roomid)){
            jsonMessage = ListCommand.buildRoomList(chatManager,null, null);
            System.out.println("Send: " + jsonMessage);

            // the server will send a delete msg to the owner first, then move the owner to the main hall
            if (serverConnection.getCurrentChatRoom().equals(roomid)){
                JoinCommand j = new JoinCommand("MainHall");
                j.execute(serverConnection);
            }
            System.out.println("delete successfully ");
        }else{
            jsonMessage = ListCommand.buildRoomList(chatManager,null, this.roomid );
            System.out.println("Send: " + jsonMessage);
            System.out.println("delete  unsuccessfully ");
        }
        chatManager.sendToOneClient(jsonMessage, serverConnection);

    }
}
