package server;

import client.ChatClient;
import client.LocalCommandHandler;
import org.apache.commons.cli.*;
import server.ChatManager;
import server.ChatServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChatPeer {
    private static final int DEFAULT_PORT = 4444;
    private static final ChatManager chatManager = new ChatManager();
    private static ChatClient chatClient = null;

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

        System.out.println("Server IP: " + ChatServer.formatIPAddr(ipAddress) + " | listeningPort: " + listeningPort + " | iPort: " + iPort);

        new ChatServer(chatManager, listeningPort).start();

        String localServerHost = ChatServer.formatIPAddr(ipAddress) + ":" + listeningPort;
        chatClient = new ChatClient(localServerHost, iPort);

        new LocalCommandHandler(chatClient, chatManager).start();
    }

}
