package client_command;

import client.ChatClient;

import java.util.ArrayList;

public class NeighborCommand extends ClientCommand {
    private ArrayList<String> neighbors;

    public NeighborCommand(ArrayList<String> neighbors){
        this.neighbors = neighbors;
    }

    @Override
    public void execute(ChatClient chatClient) {

        chatClient.getChatManager().setRecvNeighbors(neighbors);

        if (chatClient.getListNeighborCalled()){
            if (!chatClient.isConnectedLocally()){
                System.out.println();
            }
            System.out.println("neighbors: " + neighbors);
        }

        chatClient.setListNeighborCalled(false);

        if (!chatClient.isConnectedLocally()){
            chatClient.printPrefix();
        }
    }
}
