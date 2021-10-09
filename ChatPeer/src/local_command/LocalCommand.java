package local_command;

import client.ChatClient;
import server.ChatManager;

public abstract class LocalCommand {
    public abstract void execute(ChatClient chatClient, ChatManager chatManager);
}
