package local_command;

import client.ChatClient;
import server.ChatManager;

public class CreateRoomCommand extends LocalCommand {
    private String roomid;
    private final String type = "createroom";

    public CreateRoomCommand(String roomid){
        this.roomid = roomid;
    }

    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager) {
        boolean result = chatManager.createRoom(this.roomid);
        if (result) {
            System.out.println("Room " + roomid + " created.");
        } else {
            System.out.println("Room " + roomid + " is invalid or already in use.");
        }
        chatClient.printPrefix();
    }
}
