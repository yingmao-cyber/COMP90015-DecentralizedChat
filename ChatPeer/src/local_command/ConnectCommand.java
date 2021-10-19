package local_command;

import client.ChatClient;
import client_command.RoomChangeCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.LocalPeerConnection;

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
       Gson gson = new Gson();
       try {
           if (chatClient.isConnectedLocally() && !chatClient.getRoomid().equals("")){
                /** Before connect, if local peer has joined a room on local server,
                 * when it connects to another peer, this peer should leave the room and broadcast message to its room,
                 * to achieve this, a RoomChange command is sent before connection is made
                 * */
               LocalPeerConnection localPeerConnection = chatClient.getLocalPeerConnection();
               if (localPeerConnection != null){

                   RoomChangeCommand roomChangeCommand = new RoomChangeCommand(
                           localPeerConnection.getName(), localPeerConnection.getCurrentChatRoom(), "");
                   String jsonMessage = gson.toJson(roomChangeCommand);

                   if (chatClient.getRoomid().equals("")){
                       chatManager.sendToOneClient(jsonMessage, localPeerConnection);
                   }else {
                       chatManager.broadCastToCurrentRoom(localPeerConnection, jsonMessage, null);
                   }
               }
           }

           chatClient.makeConnection(remoteServerHost, specifiedLocalPort);
       }  catch (IOException e){
           e.printStackTrace();
       }

    }
}
