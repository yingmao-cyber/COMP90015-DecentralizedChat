package server_command;

import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;

public class HelpCommand  extends ServerCommand {
    private String type = "help";

    @Override
    public void execute(IConnection connection) {
        Gson gson = new Gson();

        ChatManager chatManager = connection.getChatManager();
        client_command.HelpCommand helpCommand = new client_command.HelpCommand();
        String jsonMessage = gson.toJson(helpCommand);
        chatManager.sendToOneClient(jsonMessage, connection);
    }
}
