package client_command;

import client.ChatClient;
import com.google.gson.Gson;
import server.ServerConnection;

import java.io.IOException;

public abstract class ClientCommand {
    /**
     * The detailed implementation is left to the child class to implement
     * @param chatClient
     */
    public abstract void execute(ChatClient chatClient) throws IOException;

}
