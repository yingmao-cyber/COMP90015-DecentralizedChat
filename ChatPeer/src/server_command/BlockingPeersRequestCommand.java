package server_command;

import client_command.BlockingPeersCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;

import java.util.ArrayList;
import java.util.HashSet;

public class BlockingPeersRequestCommand extends ServerCommand{
    private String type = "blockingpeersrequest";

    @Override
    public void execute(IConnection connection) {
        System.out.println("Recv blocking peer command");
        Gson gson = new Gson();
        ArrayList<String> blockingPeers = new ArrayList<>();
        HashSet<String> blockingList = connection.getChatServer().getBlockList();
        for (String peer:blockingList ){
            blockingPeers.add(peer);
        }
        BlockingPeersCommand blockingPeersCommand= new BlockingPeersCommand(blockingPeers);
        String jsonMessage = gson.toJson(blockingPeersCommand);
        connection.getChatManager().sendToOneClient(jsonMessage, connection);

    }


}
