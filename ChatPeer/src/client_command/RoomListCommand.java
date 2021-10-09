package client_command;

import client.ChatClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomListCommand extends ClientCommand{

    private List<RoomInfo> rooms = new ArrayList<>();
    private String type = "roomlist";

    public RoomListCommand(HashMap<String, Integer> info) {
        for (Map.Entry<String, Integer> entry : info.entrySet()){
            this.rooms.add(new RoomInfo(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    public void execute(ChatClient chatClient) {
        String requestNewRoomId = chatClient.getRoomToCreate();
        String requestDeleteRoomId = chatClient.getRoomToDelete();

        if (requestNewRoomId != null){
            boolean isFound = false;
            for(RoomInfo r: rooms){
                if (r.getRoomid().equals(requestNewRoomId)){
                    chatClient.processNewRoomResponse();
                    System.out.println("Room " + requestNewRoomId + " created.");
                    isFound = true;
                    break;
                }
            }
            if (! isFound) {
                chatClient.processNewRoomResponse();
                System.out.println("Room " + requestNewRoomId + " is invalid or already in use.");
            }
        }
        else if (requestDeleteRoomId != null){
            boolean isNotInList = true;
            chatClient.processRoomToDelete();
            for(RoomInfo r: rooms){
                if (r.getRoomid().equals(requestDeleteRoomId)){
                    System.out.println("Room " + requestDeleteRoomId + " failed to delete.");
                    isNotInList = false;
                    break;
                }
            }
            if (isNotInList){
                System.out.println("Room " + requestDeleteRoomId + " delete successfully.");
            }
        }
        else{
            StringBuilder print = new StringBuilder();
            for(RoomInfo r: rooms){
                int noOfGuests = r.getCount();
                print.append(r.getRoomid()).append(": ").append(noOfGuests).append(noOfGuests >1? " guests\n": " guest\n");
            }
            String printStr = print.toString().stripTrailing();
            System.out.println(printStr);
        }

        if (chatClient.isBundleMsg()){
            // since this is the last msg in the bundle, set back to false
            chatClient.setBundleMsg(false);
        }
        chatClient.printPrefix();
    }


    public class RoomInfo {
        private String roomid;
        private int count;

        public RoomInfo(String roomid, int count) {
            this.roomid = roomid;
            this.count = count;
        }
        public String getRoomid() {
            return roomid;
        }

        public int getCount() {
            return count;
        }
    }

}
