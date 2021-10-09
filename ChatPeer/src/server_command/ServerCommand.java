package server_command;

import server.ServerConnection;

public abstract class ServerCommand {
    /**
     * The detailed implementation is left to the child class to implement
     * @param serverConnection
     */
    public abstract void execute(ServerConnection serverConnection);
}
