package server;

import client.ChatClient;
import client.LocalCommandHandler;
import client_command.NewIdentityCommand;
import com.google.gson.Gson;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Options;


import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ChatServer extends Thread{
    private final int listeningPort;
    private static final int DEFAULT_PORT = 4444;
    private static final ChatManager chatManager = new ChatManager();
    private static final CommandFactory commandFactory = new CommandFactory();
    private boolean alive;
    private final Gson gson;
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());

    public ChatServer(int listeningPort){
        this.listeningPort = listeningPort;
        this.gson = new Gson();
    }

    /**
     * Valid command line input:
     * java -jar chatserver.jar -p port
     * -p port is optional
     * */
    public static void main(String[] args) throws UnknownHostException {
        int listeningPort;
        int iPort;
        String ipAddress = InetAddress.getLocalHost().toString();
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        options.addOption("p", "p", true, "listening port");
        options.addOption("i", "i", true, "local port");

        try {
            CommandLine commandLine = parser.parse(options, args);
            String serverPortNum = commandLine.getOptionValue("p");
            String localPortNum = commandLine.getOptionValue("i");

            if (serverPortNum != null){
                listeningPort = Integer.parseInt(serverPortNum);
            } else {
                listeningPort = DEFAULT_PORT;
            }
            if (localPortNum != null){
                iPort = Integer.parseInt(localPortNum);
            } else {
                /** local port placeholder, referring no input */
                iPort = -1;
            }
        } catch (ParseException e){
            e.printStackTrace();
            listeningPort = DEFAULT_PORT;
            iPort = -1;
        }

        LOGGER.info("Server IP: " + formatIPAddr(ipAddress) + " | listeningPort: " + listeningPort + " | iPort: " + iPort);

        new ChatServer(listeningPort).start();

        String localServerHost = formatIPAddr(ipAddress) + ":" + listeningPort;
        ChatClient chatClient = new ChatClient(localServerHost, iPort);

        new LocalCommandHandler(chatClient).start();
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
