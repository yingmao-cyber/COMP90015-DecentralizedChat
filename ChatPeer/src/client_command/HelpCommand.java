package client_command;

import client.ChatClient;

public class HelpCommand extends ClientCommand{
    private String type = "help";

    @Override
    public void execute(ChatClient chatClient) {
        System.out.println();
        System.out.println("#help - list this information");
        System.out.println("#connect IP[:port] [local port] - connect to another peer");
        System.out.println("#quit - disconnect from a peer");
        chatClient.printPrefix();
    }
}
