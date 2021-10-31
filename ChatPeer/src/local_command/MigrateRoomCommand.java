package local_command;

import client.ChatClient;
import client_command.MigrateToNewPeerCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.IConnection;
import server_command.BlockingPeersRequestCommand;
import server_command.ListNeighborCommand;
import server_command.MigrationRequestCommand;
import server_command.QuitCommand;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * This command will be created and execute when the user issues #migrateroom
 */
public class MigrateRoomCommand extends LocalCommand{
    private final String type = "migrateroom";
    private String roomid;

    public MigrateRoomCommand(String roomid) {
        this.roomid = roomid;
    }

    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager) throws IOException, InterruptedException {

        // check if the userpeer is connected to another peer
        if (chatClient.getRemoteServerHost() == null){
            System.out.println("You don't have neighbors to migrate the room, connect to a peer first");
            return;
        }

        // check if the room exists
        if (!chatManager.hasChatRoom(roomid)){
            System.out.println("You don't have this room");
            return;
        }
        Gson gson = new Gson();
        String migrateCandidate = chatClient.getRemoteServerHost();

        //run the connection in the background

//        c.setMigrating(true);
        boolean migrateSuccessfully = false;

        //send blockingPeersRequest to see if the candidate is valud
        if (migrateCandidate != null){
            chatManager.clearRecvServerBlockingPeers();
            BlockingPeersRequestCommand blockingPeersRequestCommand = new BlockingPeersRequestCommand();
            chatClient.getWriter().println(gson.toJson(blockingPeersRequestCommand));
            if (isValidCandidate(chatManager)){
                migrateSuccessfully = tryMigration(migrateCandidate, chatManager, chatClient);
            }
        }
        System.out.println(migrateSuccessfully);

        // if the migration is failed, send a listNeighbourRequest to see if there are other candidates

        if (!migrateSuccessfully){

            ListNeighborCommand listNeighborCommand = new ListNeighborCommand();
            chatClient.getWriter().println(gson.toJson(listNeighborCommand));
            try{
                ArrayList<String> candidates = chatManager.getRecvNeighbors();
                synchronized (candidates){
                    chatManager.getRecvNeighbors().wait(5000);
                }
            } catch (InterruptedException e){
                e.printStackTrace();
            }

            ArrayList<String> peerNeighbors = chatManager.getRecvNeighbors();
            ChatClient c = new ChatClient(chatManager, chatClient.getLocalServerHost(), -1);
            c.setRunningInBackground(true);
            for (String s: peerNeighbors){
                c.makeConnection(s, -1);
                chatManager.clearRecvServerBlockingPeers();
                BlockingPeersRequestCommand blockingPeersRequestCommand = new BlockingPeersRequestCommand();
                c.getWriter().println(gson.toJson(blockingPeersRequestCommand));
                if (isValidCandidate(chatManager)){
                    if(tryMigration(migrateCandidate, chatManager, c)){
                        System.out.println("migrate successfully");
                        QuitCommand q = new QuitCommand();
                        c.setQuitFlag(true);
                        c.getSocket().close();
                        c.getWriter().println(gson.toJson((q)));
                        c.getClientReceiver().setConnection_alive(false);
                        return;

                    }
                }
                QuitCommand q = new QuitCommand();
                c.setQuitFlag(true);
                c.getSocket().close();
                c.getWriter().println(gson.toJson((q)));
                c.getClientReceiver().setConnection_alive(false);
            }

            System.out.println("No suitable candidate to migrate the room " + roomid);
        }
    }

    public boolean isValidCandidate(ChatManager chatManager){
        try{
            ArrayList<String> blockingList = chatManager.getRecvServerBlockingPeers();
            synchronized (blockingList){
                chatManager.getRecvServerBlockingPeers().wait(5000);
            }
            if (!chatManager.isReceivedBlockingPeers()){
                return false;
            }else{

                ArrayList<IConnection> peersInRoom = chatManager.getChatRooms(roomid);
                // check if the candidate's blocking list contains peer in the room
                for (IConnection c : peersInRoom){
                    String[] peerId = c.getName().split(":");
                    String peerIP = peerId[0];
                    if (blockingList.contains(peerIP)){
                        chatManager.clearRecvServerBlockingPeers();
                        chatManager.setReceivedBlockingPeers(false);
                        return false;
                    }
                }
                chatManager.clearRecvServerBlockingPeers();
                chatManager.setReceivedBlockingPeers(false);
                return true;
            }
        } catch (InterruptedException e){
            e.printStackTrace();
            return false;
        }
    }
    public boolean tryMigration(String candidate, ChatManager chatManager, ChatClient c) throws InterruptedException, IOException {
        MigrationRequestCommand migrationRequestCommand = new MigrationRequestCommand(roomid);
        Gson gson = new Gson();
        String s = gson.toJson(migrationRequestCommand);
        System.out.println(s);
        PrintWriter w = c.getWriter();
        w.println(s);
        int count = 0;
        while(!chatManager.isCurrentMigrationSuccessful() && count < 5){
            Thread.sleep(1000);
            count +=1;
        }
        if (!chatManager.isCurrentMigrationSuccessful()){
            return false;
        }else{
            chatManager.setCurrentMigrationSuccessful(false);
            chatManager.setConnectionFailedAlert(false);
            ArrayList<IConnection> peersInRoom = chatManager.getChatRooms(roomid);
            for (IConnection peer: peersInRoom){
                c.makeConnection(peer.getName(), -1);
                MigrateToNewPeerCommand migrateToNewPeerCommand = new MigrateToNewPeerCommand( candidate,roomid,c.getLocalServerHost());
                QuitCommand q = new QuitCommand();
                c.setQuitFlag(true);
                c.getSocket().close();
                c.getWriter().println(gson.toJson((q)));
                c.getClientReceiver().setConnection_alive(false);
            }
            count = 0;
            while (!chatManager.isConnectionFailedAlert() && count < 5){
                Thread.sleep(1000);
                count ++;
            }
            return !chatManager.isConnectionFailedAlert();
        }
    }

}
