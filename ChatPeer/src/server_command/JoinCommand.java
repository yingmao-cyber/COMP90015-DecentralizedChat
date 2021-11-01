package server_command;

import client_command.RoomChangeCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;

public class JoinCommand extends ServerCommand{
    private String roomid;
    private final String type = "join";

    public JoinCommand(String roomid){
        this.roomid = roomid;
    }

    @Override
    public void execute(IConnection connection) {
        Gson gson = new Gson();
        String identity = connection.getName();
        String former = connection.getCurrentChatRoom();

        ChatManager chatManager = connection.getChatManager();

        /**
         * if join room successfully
         * - server responds roomchange message to all currently connected clients in the same room
         * if to mainhall
         * - server responds roomchange message with a roomlist message to all currently connected clients in the same room
         * -otherwise
         * server responds message only to the client
         */
        String jsonMessage;
        RoomChangeCommand roomChangeCommand;
        if (!this.roomid.equals(former) && chatManager.joinRoom(connection, roomid)){
            roomChangeCommand = new RoomChangeCommand(identity, former, roomid);
            jsonMessage = gson.toJson(roomChangeCommand);
//            System.out.println("Send: " + jsonMessage);
            // if current room is not null, also send to the current room
            if (!connection.getCurrentChatRoom().equals("")){
                chatManager.broadCastToCurrentRoom(connection, jsonMessage, null);
            }

            //send to all the clients in the room the client move to
            connection.setCurrentChatRoom(roomid);
            chatManager.broadCastToCurrentRoom(connection, jsonMessage, null);

        }
        else{ // if requested room is the current room or the requested does not exist, the request is invalid
            if (!roomid.equals("")){
                roomChangeCommand = new RoomChangeCommand(identity, former, null);
                jsonMessage = gson.toJson(roomChangeCommand);
//            System.out.println("Send: " + jsonMessage);
                chatManager.sendToOneClient(jsonMessage, connection);
            }
        }

    }
}
