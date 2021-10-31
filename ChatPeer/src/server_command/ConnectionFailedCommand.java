package server_command;

import server.IConnection;

public class ConnectionFailedCommand extends ServerCommand{
    private final String type = "connectionfailed";
    private String failedPeer;
    private String roomid;

    public ConnectionFailedCommand(String failedPeer , String roomid) {
        this.failedPeer = failedPeer;
        this.roomid = roomid;
    }

    @Override
    public void execute(IConnection connection) {
        connection.getChatManager().addConnectionFailedPeer(failedPeer);
        connection.getChatManager().setConnectionFailedAlert(true);
    }
}
