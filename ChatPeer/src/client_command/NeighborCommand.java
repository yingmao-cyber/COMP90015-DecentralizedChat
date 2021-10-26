package client_command;

import client.ChatClient;

import java.util.ArrayList;
import java.util.List;

public class NeighborCommand extends ClientCommand {
    private ArrayList<String> neighbors;

    public NeighborCommand(ArrayList<String> neighbors){
        this.neighbors = neighbors;
    }

    @Override
    public void execute(ChatClient chatClient) {

        chatClient.getChatManager().setRecvNeighbors(neighbors);

        if (!chatClient.isConnectedLocally()){
            System.out.println();
        }
        if (!chatClient.isConnectedLocally()){
            chatClient.printPrefix();
        }
    }
}
