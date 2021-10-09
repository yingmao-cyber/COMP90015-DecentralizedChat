package local_command;

import client.ChatClient;
import server.ChatManager;

import java.io.IOException;

public class ConnectCommand extends LocalCommand {
    private String remoteServerHost;
    private int specifiedLocalPort;

   public ConnectCommand(String remoteServerHost, int specifiedLocalPort){
       this.remoteServerHost = remoteServerHost;
       this.specifiedLocalPort = specifiedLocalPort;
   }

    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager){
       try {
           chatClient.makeConnection(remoteServerHost, specifiedLocalPort);
       }  catch (IOException e){
           e.printStackTrace();
       }

    }
}
