package client_command;

import client.ChatClient;

/**
 * This class is used when client first connect to server or client requests for an identity change
 * Request message:
 * {"type": "newidentity", "former": "", "identity": "guest3"}
 */
public class NewIdentityCommand extends ClientCommand {
    private String former;
    private String identity;
    private final String type = "newidentity";

    public NewIdentityCommand(String former, String identity){
        this.former = former;
        this.identity = identity;
    }

    @Override
    public void execute(ChatClient chatClient) {
        boolean firstTimeConnection = former.equals("");
        String str;

        if (firstTimeConnection){
            chatClient.setIdentity(identity);
            str = "Connected to localhost as " + identity;
        } else {
            str = performIdentityChange(chatClient);
        }

        System.out.println(str);

        if (!firstTimeConnection){
            chatClient.printPrefix();
        }
    }

    private String performIdentityChange(ChatClient chatClient){

        if (former.equals(identity)) {
            return "Requested identity invalid or in use";
        } else if (former.equals(chatClient.getIdentity())) {
            // if former is equal to chatClient identity, this means that this client has changed the identity
            chatClient.setIdentity(identity);
        }
        return former + " is now " + identity;
    }

}
