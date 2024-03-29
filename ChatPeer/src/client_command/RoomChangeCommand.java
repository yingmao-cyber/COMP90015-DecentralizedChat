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
        /** To distinguish an invalid room request from empty room, null is used.*/
        if (roomid == null){
            if (!chatClient.isConnectedLocally()){
                System.out.println();
            }
            System.out.println("The requested room is invalid or non existent");
            if (!chatClient.isConnectedLocally()){
                chatClient.printPrefix();
            }
            return;
        }

//        System.out.println("former: " + former + ", identity: " + identity + " | " +  chatClient.getIdentity() + ", " + "roomid: " + roomid);
        /** room id changed to " " */
        if (roomid.equals("") && (!former.equals(roomid))){
            System.out.println(identity + " leaves " + chatClient.getRoomid());
           if (identity.equals(chatClient.getIdentity()) && chatClient.getQuitFlag()){
                try {
                    chatClient.setRoomid("");
                    chatClient.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            else if (identity.equals(chatClient.getIdentity())) {
                /** for room deletion */
               chatClient.setRoomid("");
               if (!chatClient.isConnectedLocally()){
                   chatClient.printPrefix();
               }
               return;
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
            if (chatClient.getQuitFlag()){
                try {
                    System.out.println(identity + " leaves " + chatClient.getRoomid());
                    chatClient.setRoomid("");
                    chatClient.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
            chatClient.setIdentity(identity);
        }

        if (!chatClient.isBundleMsg() && !chatClient.isConnectedLocally()){
            chatClient.printPrefix();
        }
    }
}
