package server_command;

import server.IConnection;
import server.LocalPeerConnection;
import server.ServerConnection;

public abstract class ServerCommand {
    /**
     * The detailed implementation is left to the child class to implement
     * @param serverConnection
     */
    public abstract void execute(IConnection connection);
}
