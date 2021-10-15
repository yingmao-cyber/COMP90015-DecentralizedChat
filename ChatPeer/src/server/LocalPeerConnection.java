package server;


import client.ChatClient;
import client_command.ClientCommand;


public class LocalPeerConnection implements IConnection{
    private String currentChatRoom = "";
    private String identity = "";
    private final String connType = "local";
    private ChatManager chatManager;
    private client.CommandFactory clientCommandFactory;
    private ChatClient chatClient;

    public LocalPeerConnection(ChatClient chatClient,
                               ChatManager chatManager, client.CommandFactory clientCommandFactory, String identity){
        this.chatClient = chatClient;
        this.identity = identity;
        this.chatManager = chatManager;
        this.clientCommandFactory = clientCommandFactory;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    @Override
    public String getConnType() {
        return connType;
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

    public void sendMessage(String message){
        ClientCommand clientCommand = clientCommandFactory.convertServerMessageToCommand(message);
        if (clientCommand != null){
            clientCommand.execute(chatClient);
        }
    }

    public void close() {
        chatManager.removeClientConnection(this);
    }

}
