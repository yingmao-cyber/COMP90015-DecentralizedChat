package client;

import client_command.RoomChangeCommand;
import local_command.ConnectCommand;
import local_command.LocalCommand;
import local_command.LocalCommandFactory;
import server.ChatManager;
import server.LocalPeerConnection;
import server_command.ServerCommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalCommandHandler extends Thread {
    private boolean connected = true;
    private final ChatManager chatManager;
    private final ChatClient chatClient;
    private BufferedReader userInput;
    private LocalCommandFactory localCommandFactory;
    private LocalPeerConnection localPeerConnection;
    private final client.CommandFactory clientCommandFactory;

    public LocalCommandHandler(ChatClient chatClient, ChatManager chatManager, CommandFactory clientCommandFactory,
                               LocalPeerConnection localPeerConnection){
        this.chatManager = chatManager;
        this.chatClient = chatClient;
        this.clientCommandFactory = clientCommandFactory;
        this.localPeerConnection = localPeerConnection;
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
        this.localCommandFactory = new LocalCommandFactory(chatClient);

    }


    public void run(){
        connected = true;
        chatClient.printPrefix();
        while (connected) {
            try {
                boolean isConnectedToLocal = chatManager.isClientInConnectionList(localPeerConnection);
                if (!isConnectedToLocal){
                    RoomChangeCommand roomChangeCommand = new RoomChangeCommand(localPeerConnection.getName(), "", "n");
                    roomChangeCommand.execute(chatClient);
                    chatManager.addClientToConnectionList(localPeerConnection, localPeerConnection.getName());
                }

                String str = userInput.readLine();
                if (str != null){
                    LocalCommand localCommand = localCommandFactory.convertUserInputToCommand(str);
                    ServerCommand serverCommand = clientCommandFactory.convertUserInputToCommand(str);
                    if (localCommand != null){
                        localCommand.execute(chatClient, chatManager);
                    } else if (serverCommand != null){
                        if (str.contains("#create")){
                            System.out.println("Need to quit first to be able to connect to new peer.");
                        }
                        serverCommand.execute(localPeerConnection);
                    }
                }
            } catch (IOException e) {
                connected = false;
                e.printStackTrace();
            }
        }
    }
}
