package server;

import client_command.NewIdentityCommand;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class ChatServer extends Thread{
    private final int listeningPort;
    private final ChatManager chatManager;

    private static final CommandFactory commandFactory = new CommandFactory();
    private boolean alive;
    private final Gson gson;
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());

    public ChatServer(ChatManager chatManager, int listeningPort){
        this.chatManager = chatManager;
        this.listeningPort = listeningPort;
        this.gson = new Gson();
    }

    public static String formatIPAddr(String ipAddr){
        int index = ipAddr.indexOf("/") + 1;
        return ipAddr.substring(index);
    }

    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(listeningPort);
            alive = true;

            while (alive){
                Socket soc = serverSocket.accept();

                String socAddr = soc.getRemoteSocketAddress().toString();
                String peerIdentity = formatIPAddr(socAddr);

                if (soc != null){
                    LOGGER.info("New connection received: " + soc.getRemoteSocketAddress().toString());
                    ServerConnection serverConnection = new ServerConnection(soc, chatManager, commandFactory);
                    serverConnection.start(); // start thread
                    serverConnection.setName(peerIdentity);
                    chatManager.addClientToConnectionList(serverConnection, null);
                    String peerIdentityMessage = gson.toJson(new NewIdentityCommand(peerIdentity));
                    chatManager.sendToOneClient(peerIdentityMessage, serverConnection);
                }

            }
        } catch (IOException e) {
            alive = false;
            e.printStackTrace();
        }
    }
}
