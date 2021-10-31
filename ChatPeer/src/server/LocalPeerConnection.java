package server;


import client.ChatClient;
import client_command.ClientCommand;

import java.io.IOException;


public class LocalPeerConnection implements IConnection{
    private String currentChatRoom = "";
    private String identity = "";
    private final String connType = "local";
    private ChatManager chatManager;
    private client.CommandFactory clientCommandFactory;
    private ChatClient chatClient;
    private ChatServer chatServer;

    public LocalPeerConnection(ChatClient chatClient,
                               ChatManager chatManager, client.CommandFactory clientCommandFactory, String identity, ChatServer c){
        this.chatClient = chatClient;
        this.identity = identity;
        this.chatManager = chatManager;
        this.clientCommandFactory = clientCommandFactory;
        this.chatServer = c;
    }


    public ChatManager getChatManager() {
        return chatManager;
    }

    @Override
    public String getConnType() {
        return connType;
    }

    @Override
    public ChatServer getChatServer() {
        return chatServer;
    }


    public String getName(){
        return this.identity;
    }

    public String getCurrentChatRoom(){
        return currentChatRoom;
    }

    public void setName(String identity) {
        this.identity = identity;
    }

    public void setCurrentChatRoom(String currentChatRoom) {
        this.currentChatRoom = currentChatRoom;
    }

    public void sendMessage(String message) throws IOException {
        ClientCommand clientCommand = clientCommandFactory.convertServerMessageToCommand(message);
        if (clientCommand != null){
            clientCommand.execute(chatClient);
        }
    }

    public void close() {
        chatManager.removeClientConnection(this);
    }

}
