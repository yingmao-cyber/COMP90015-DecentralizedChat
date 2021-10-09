package client_command;

import client.ChatClient;

/**
 * Return peer's ip and port seen by remote server from server to client
 * Request message:
 * {"type": "newidentity", "identity": "xxx.xxx.x.xxx:localport"}
 */
public class NewIdentityCommand extends ClientCommand {
    private String identity;
    private final String type = "newidentity";

    public NewIdentityCommand(String identity){
        this.identity = identity;
    }

    @Override
    public void execute(ChatClient chatClient) {
        chatClient.setIdentity(identity);
        chatClient.printPrefix();
    }
}
