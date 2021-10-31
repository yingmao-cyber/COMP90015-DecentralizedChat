package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server_command.*;

import java.lang.reflect.Type;

public class CommandFactory {
    private final Gson gson;

    private ServerCommand generateCommand(String jsonMessage, Class commandClass){
        return this.gson.fromJson(jsonMessage, (Type) commandClass);
    }

    public CommandFactory(){
        this.gson = new Gson();
    }

    /**
     * Convert json message sent by client at server side to command object
     * @param jsonMessage
     * @return
     */
    public ServerCommand convertClientMessageToCommand(String jsonMessage){
        String type = this.gson.fromJson(jsonMessage, JsonObject.class).get("type").getAsString();

        switch(type){
            case "hostchange":
                return this.generateCommand(jsonMessage, HostChangeCommand.class);
            case "message":
                return this.generateCommand(jsonMessage, MessageCommand.class);
            case "listneighbors":
                return this.generateCommand(jsonMessage, ListNeighborCommand.class);
            case "join":
                return this.generateCommand(jsonMessage, JoinCommand.class);
            case "help":
                return this.generateCommand(jsonMessage, HelpCommand.class);
            case "who":
                return this.generateCommand(jsonMessage, WhoCommand.class);
            case "list":
                return this.generateCommand(jsonMessage, ListCommand.class);
            case "quit":
                return this.generateCommand(jsonMessage, QuitCommand.class);
            case "blockingpeersrequest":
                return this.generateCommand(jsonMessage, BlockingPeersRequestCommand.class);
            case "connectionfailed":
                return this.generateCommand(jsonMessage, ConnectionFailedCommand.class);
            case "migrationrequest":
                return this.generateCommand(jsonMessage, MigrationRequestCommand.class);
            default:
                return null;
        }
    }

}
