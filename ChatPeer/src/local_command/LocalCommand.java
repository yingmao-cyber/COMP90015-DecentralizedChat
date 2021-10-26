package local_command;

import client.ChatClient;
import server.ChatManager;

import java.io.IOException;

public abstract class LocalCommand {
    public abstract void execute(ChatClient chatClient, ChatManager chatManager) throws IOException, InterruptedException;
}
