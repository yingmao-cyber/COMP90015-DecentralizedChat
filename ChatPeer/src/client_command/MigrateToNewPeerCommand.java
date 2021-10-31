package client_command;

import client.ChatClient;
import com.google.gson.Gson;
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
    public void execute(ChatClient chatClient) throws IOException {
        Gson gson = new Gson();
        System.out.println("Due to Server request, start migrating to new Peer ... ");
        QuitCommand q = new QuitCommand();
        chatClient.setQuitFlag(true);
        chatClient.getWriter().println(gson.toJson((q)));
        chatClient.disconnect();
        chatClient.setMigrating(true);

        try{
            chatClient.makeConnection(peerAddress, Integer.parseInt(chatClient.getLocalPort()));
            JoinCommand joinCommand = new JoinCommand(roomid);
            chatClient.getWriter().println(gson.toJson((joinCommand)));
        }catch(IOException e){
            chatClient.setQuitFlag(true);
            chatClient.disconnect();
            chatClient.makeConnection(currentConnectedPeerAddress,Integer.parseInt(chatClient.getLocalPort()) );
            ConnectionFailedCommand connectionFailedCommand = new ConnectionFailedCommand(peerAddress, roomid);
            chatClient.getWriter().println(gson.toJson((connectionFailedCommand)));
            chatClient.setMigrating(false);
        }

    }
}
