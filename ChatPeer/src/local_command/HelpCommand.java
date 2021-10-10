package local_command;

import client.ChatClient;
import server.ChatManager;

public class HelpCommand extends LocalCommand{
    private String type = "help";

    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager) {
        System.out.println("#help - list this information");
        System.out.println("#connect IP[:port] [local port] - connect to another peer");
        System.out.println("#quit - disconnect from a peer");
    }
}
