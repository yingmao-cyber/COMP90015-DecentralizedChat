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
        if (roomid == null){
            System.out.println("\nThe requested room is invalid or non existent");
            chatClient.printPrefix();
            return;
        }
        /** for room deletion */
        if (roomid.equals("d")){
            chatClient.setRoomid("");
            System.out.println();
            if (!chatClient.isConnectedLocally()){
                chatClient.printPrefix();
            }
            return;
        }
        /** for the initial new room */

//        System.out.println("former: " + former + ", identity: " + identity + ", roomid: " + roomid);
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

        }else if (former.equals("") && roomid.equals("")){
            chatClient.setIdentity(identity);
        }

        if (!chatClient.isBundleMsg() ){
            chatClient.printPrefix();
        }
    }
}
