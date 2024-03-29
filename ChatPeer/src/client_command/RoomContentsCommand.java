package client_command;

import client.ChatClient;

import java.util.ArrayList;
import java.util.List;

public class RoomContentsCommand extends ClientCommand{

    private String type = "roomcontents";
    private String roomId;
    private List<String> identities;

    public RoomContentsCommand(String roomId, List<String> identities) {
        this.roomId = roomId;
        this.identities = identities;
    }

    @Override
    public void execute(ChatClient chatClient) {
        StringBuilder print = new StringBuilder(roomId + " contains ");

        // if no one in the room, displays "room is empty"
        if (identities.size() == 0 ){
            System.out.println(roomId + " is empty.");
            if (!chatClient.isConnectedLocally()){
                System.out.println();
                chatClient.printPrefix();
            }
            return;
        }
        else {
            //otherwise, display the room information
            for(String str: identities){
                print.append(str).append(" ");
            }
        }
        if (!chatClient.isConnectedLocally()){
            System.out.println();
        }
        System.out.println(print);

        if (!chatClient.isBundleMsg() && !chatClient.isConnectedLocally()){
            chatClient.printPrefix();
        }

    }
}
