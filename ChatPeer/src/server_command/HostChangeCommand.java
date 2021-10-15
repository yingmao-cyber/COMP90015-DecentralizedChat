package server_command;

import server.ChatManager;
import server.IConnection;

public class HostChangeCommand extends ServerCommand {
    private String host;
    private final String type = "hostchange";

    public HostChangeCommand(String host){
        this.host = host;
    }

    @Override
    public void execute(IConnection connection) {
        if (connection.getConnType().equals("remote")){
            ChatManager chatManager = connection.getChatManager();
            /** host is referring to the peer's server host; this list is maintained for list neighbours request */
            chatManager.addClientToConnectionList(connection, host);
        }
    }
}
