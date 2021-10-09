package local_command;

import client.ChatClient;

import java.io.IOException;

public class ConnectCommand extends LocalCommand {
    private String remoteServerHost;
    private int specifiedLocalPort;

   public ConnectCommand(String remoteServerHost, int specifiedLocalPort){
       this.remoteServerHost = remoteServerHost;
       this.specifiedLocalPort = specifiedLocalPort;
   }

    @Override
    public void execute(ChatClient chatClient){
       try {
           chatClient.makeConnection(remoteServerHost, specifiedLocalPort);
       }  catch (IOException e){
           e.printStackTrace();
       }

    }
}
