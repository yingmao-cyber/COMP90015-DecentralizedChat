package server;

import client_command.RoomChangeCommand;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Logger;

public class ChatServer extends Thread{
    private final int listeningPort;
    private final ChatManager chatManager;

    private static final CommandFactory commandFactory = new CommandFactory();
    private boolean alive;
    private final Gson gson;
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    private HashSet<String> blockList;

    public ChatServer(ChatManager chatManager, int listeningPort){
        this.chatManager = chatManager;
        this.listeningPort = listeningPort;
        this.gson = new Gson();
        this.chatManager.setChatServer(this);
        this.blockList = new HashSet<>();
    }

    public static String formatIPAddr(String ipAddr){
        int index = ipAddr.indexOf("/") + 1;
        return ipAddr.substring(index);
    }

    public void blockClient (String bc){
        this.blockList.add(bc);
    }


    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(listeningPort);
            serverSocket.setReuseAddress(true);
            alive = true;
            while (alive){
                Socket soc = serverSocket.accept();
                String socAddr = soc.getRemoteSocketAddress().toString();
                String peerIdentity = formatIPAddr(socAddr);
                if (soc != null && !this.blockList.contains(peerIdentity)){
                    System.out.println("New connection received: " + soc.getRemoteSocketAddress().toString());
                    ServerConnection serverConnection = new ServerConnection(soc, chatManager, commandFactory);
                    serverConnection.start(); // start thread
                    serverConnection.setName(peerIdentity);
                    chatManager.addClientToConnectionList(serverConnection, null);
                    String peerIdentityMessage = gson.toJson(new RoomChangeCommand(peerIdentity, "", ""));
                    chatManager.sendToOneClient(peerIdentityMessage, serverConnection);
                }
                else{
                    soc.close();
                }

            }
        } catch (IOException e) {
            alive = false;
            System.out.print("Connection failed");
//            e.printStackTrace();
        }
    }
}
