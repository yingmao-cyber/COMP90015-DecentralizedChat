package client_command;

import client.ChatClient;
import com.google.gson.Gson;
import server.IConnection;
import server_command.ConnectionFailedCommand;
import server_command.JoinCommand;
import server_command.QuitCommand;

import java.io.IOException;

public class MigrateToNewPeerCommand extends ClientCommand{
    private final String type = "migratetonewpeer";
    private String peerAddress;
    private String roomid;
    private String currentConnectedPeerAddress;

    public MigrateToNewPeerCommand(String peerAddress, String roomid, String currentConnectedPeerAddress) {
        this.peerAddress = peerAddress;
        this.roomid = roomid;
        this.currentConnectedPeerAddress = currentConnectedPeerAddress;
    }

    @Override
    public void execute(ChatClient chatClient) throws IOException{
        Gson gson = new Gson();
        System.out.println("\nDue to Server request, start migrating to new Peer " + peerAddress + "... ");
        chatClient.setRoomid("");
        chatClient.disconnect();
        chatClient.setMigrating(true);
        while (!chatClient.isMigrated()){
            try {
                Thread.sleep(200);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        try{
            chatClient.setRoomid(roomid);
            chatClient.makeConnection(peerAddress, Integer.parseInt(chatClient.getLocalPort()));
        }catch(IOException e){
            chatClient.setRoomid("");
            chatClient.disconnect();
            chatClient.makeConnection(currentConnectedPeerAddress,Integer.parseInt(chatClient.getLocalPort()) );
            ConnectionFailedCommand connectionFailedCommand = new ConnectionFailedCommand(peerAddress, roomid);
            chatClient.getWriter().println(gson.toJson((connectionFailedCommand)));
            chatClient.setMigrating(false);
        }

    }
}
