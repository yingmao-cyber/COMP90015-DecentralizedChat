package local_command;

import client.ChatClient;

public abstract class LocalCommand {
    public abstract void execute(ChatClient chatClient);
}
