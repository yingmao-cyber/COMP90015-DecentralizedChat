package local_command;

import client.ChatClient;
import client_command.RoomListCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;
import server_command.ListCommand;
import server_command.ListNeighborCommand;
import server_command.QuitCommand;

import java.io.IOException;
import java.util.*;

public class SearchCommand extends LocalCommand{
    private final String type = "searchnetwork";
    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager) throws IOException, InterruptedException {
//        System.out.println("Start Searching");
        HashMap<IConnection, String> peers = chatManager.getClientConnectionList();
        HashMap<String, Boolean> isExplored = new HashMap<>();
        HashMap<String, String> roomSearched = new HashMap<>();
        String localServerHost = chatClient.getLocalServerHost();
        isExplored.put(localServerHost, true);
        Queue<String> searchQueue = new LinkedList();
        for (String peerAddress : peers.values()) {
            if (!peerAddress.equals(localServerHost)){
                searchQueue.add(peerAddress);
            }
        }
        while (!searchQueue.isEmpty()) {
            Gson gson = new Gson();
            String currentSearchPeer = searchQueue.poll();
            ChatClient c = new ChatClient(chatManager, chatClient.getLocalServerHost(), -1);
            c.setRunningInBackground(true); // this thread is running in background
            c.makeConnection(currentSearchPeer, -1);
//            System.out.println("Connected to " + currentSearchPeer );
            ListNeighborCommand l = new ListNeighborCommand();
            c.getWriter().println(gson.toJson(l));
            try{
                ArrayList<String> neighbors = chatManager.getRecvNeighbors();
                synchronized (neighbors){
                    chatManager.getRecvNeighbors().wait(5000);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            ArrayList<String> peerNeighbors = chatManager.getRecvNeighbors();
//            System.out.println("Receive neighbours:");
            for (String s: peerNeighbors){
//                System.out.println(s);
                if (!isExplored.getOrDefault(s, false)) {
                    isExplored.put(s, true);
                    searchQueue.add(s);
                }
            }
            chatManager.clearRecvNeighbors();
            ListCommand r = new ListCommand();
            c.getWriter().println(gson.toJson((r)));
            try{
                List<RoomListCommand.RoomInfo> roomInfos = chatManager.getRecvRoomInfo();
                synchronized (roomInfos){
                    chatManager.getRecvRoomInfo().wait(5000);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            List<RoomListCommand.RoomInfo> recvRooms = chatManager.getRecvRoomInfo();
//            System.out.println(currentSearchPeer + " Rooms:");
            StringBuilder print = new StringBuilder();
            for (RoomListCommand.RoomInfo room: recvRooms){
                int noOfGuests = room.getCount();
                if (!room.getRoomid().equals("")){
                    print.append(room.getRoomid()).append(": ").append(noOfGuests).append(noOfGuests >1? " guests\n": " guest\n");
                }
            }
            chatManager.clearRecvRoomInfos();
            String printStr = print.toString().stripTrailing();
//            System.out.println(printStr);
            roomSearched.put(currentSearchPeer, printStr);

            QuitCommand q = new QuitCommand();
            c.setQuitFlag(true);
            c.getSocket().close();
            c.getWriter().println(gson.toJson((q)));
            c.getClientReceiver().setConnection_alive(false);
        }

        for (Map.Entry<String, String> searchedInfo: roomSearched.entrySet()){
            System.out.println(searchedInfo.getKey() + " Rooms:");
            System.out.println(searchedInfo.getValue());
        }
//        System.out.println("End Search");
    }
}
