package server_command;

import client_command.MessageRelayCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;

public class MessageCommand extends ServerCommand{
    /** message sent by client: {"type": "message", "content": "Hi there!"} */
    private String content;
    private final String type = "message";

    public MessageCommand(String content){
        this.content = content;
    }

    @Override
    public void execute(ServerConnection serverConnection) {
        Gson gson = new Gson();
        ChatManager chatManager = serverConnection.getChatManager();

        /** message sent to client in the same room:
         * {"type": "message", "identity": "aaron", "content": "Hi there!"}
         * id of the sender is appended to the message
         */
        String identity = serverConnection.getName();
        MessageRelayCommand messageRelayCommand = new MessageRelayCommand(identity, this.content);
        String jsonMessage = gson.toJson(messageRelayCommand);
        System.out.println("Send: " + jsonMessage);

        chatManager.broadCastToCurrentRoom(serverConnection, jsonMessage, null);

    }
}
