package server_command;

import client_command.NeighborCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListNeighborCommand extends ServerCommand {
    private String type = "listneighbors";

    @Override
    public void execute(ServerConnection serverConnection) {
        ChatManager chatManager = serverConnection.getChatManager();
        HashMap<ServerConnection, String> peerLists = chatManager.getClientConnectionList();
        ArrayList<String> listNeighbors = new ArrayList<>();
        for (Map.Entry<ServerConnection, String> entry: peerLists.entrySet()){
            /** list should not include the address of the client that issued the request */
            if (!entry.getKey().equals(serverConnection)){
                listNeighbors.add(entry.getValue());
            }
        }
        /** list may be empty */
        Gson gson = new Gson();
        NeighborCommand neighborCommand = new NeighborCommand(listNeighbors);
        String jsonMessage = gson.toJson(neighborCommand);
        chatManager.sendToOneClient(jsonMessage, serverConnection);
    }
}
