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
        System.out.println("\nneighbors: " + this.neighbors);
    }
}
