package client_command;

import client.ChatClient;

public class MessageRelayCommand extends ClientCommand{
    private final String type = "message";
    private String identity;
    private String content;

    /** received broadcast message sent from server:
     * {"type": "message", "identity": "aaron", "content": "Hi there!"}
     */
    public MessageRelayCommand(String identity, String content){
        this.identity = identity;
        this.content = content;
    }

    @Override
    public void execute(ChatClient chatClient) {
        System.out.println(identity + ": " + content);
        chatClient.printPrefix();
    }
}
