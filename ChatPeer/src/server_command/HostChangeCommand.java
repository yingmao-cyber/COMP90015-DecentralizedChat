package server_command;

import server.ChatManager;
import server.ServerConnection;

public class HostChangeCommand extends ServerCommand {
    private String host;
    private final String type = "hostchange";

    public HostChangeCommand(String host){
        this.host = host;
    }

    @Override
    public void execute(ServerConnection serverConnection) {
        System.out.println("received host change command");
        ChatManager chatManager = serverConnection.getChatManager();
        /** host is referring to the peer's server host; this list is maintained for list neighbours request */
        chatManager.addClientConnection(serverConnection, host);
    }
}
