package client_command;

import client.ChatClient;

public class RoomChangeCommand extends ClientCommand{
    private String roomid;
    private String identity;
    private String former;
    private String type = "roomchange";

    public RoomChangeCommand(String identity, String former, String roomid){
        this.identity = identity;
        this.former   = former;
        this.roomid   = roomid;
    }

    public String getRoomid() {
        return roomid;
    }

    @Override
    public void execute(ChatClient chatClient) {
        /** room id changed to " " */
        if (roomid.equals("") && (!former.equals(roomid))){
           if (identity.equals(chatClient.getIdentity()) && chatClient.getQuitFlag()){
                try {
                    System.out.println(identity + " leaves " + chatClient.getRoomid());
                    chatClient.setRoomid("");
                    chatClient.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            else {
                System.out.println(identity + " leaves " + chatClient.getRoomid());
            }
        } else if (!former.equals(roomid)){
            if (identity.equals(chatClient.getIdentity())){
                chatClient.setRoomid(roomid);
            }
            if (former.equals("")){
                System.out.println( identity + " moves to " + roomid);
            }else{
                System.out.println(identity + " moved from " + former + " to " + roomid);
            }

        }else{
            System.out.println("\nThe requested room is invalid or non existent");
        }

        if (!chatClient.isBundleMsg()){
            chatClient.printPrefix();
        }
    }
}
