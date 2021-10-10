package client_command;

import client.ChatClient;

import java.util.ArrayList;

public class RoomChangeOnDelete extends ClientCommand{
    private ArrayList<String> deleted;
    private String type = "roomchangeondelete";

    public RoomChangeOnDelete(ArrayList<String> deleted){
        this.deleted = deleted;
    }

    /** This command is issued to make client to change their identity
     * from [rooma] xxx.xxx.xx.xx to [] xxx.xxx.xx.xx
     *  */
    @Override
    public void execute(ChatClient chatClient) {
        for (String sc: deleted){
            if (chatClient.getIdentity().equals(sc)){
                chatClient.setRoomid("");
            }
        }
    }
}
