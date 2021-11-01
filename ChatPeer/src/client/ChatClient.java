package client;

import com.google.gson.Gson;
import server.ChatManager;
import server.LocalPeerConnection;
import server_command.HostChangeCommand;
import server_command.ServerCommand;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
    private boolean quitFlag = false;
    private PrintWriter writer;
    private final String localServerHost;
    private Gson gson;
    private String identity = "";
    private boolean connected = true;
    private String roomid = "";
    private int iPort = -1;
    private String remoteServerHost = null;
    private ClientSender clientSender = null;
    private ClientReceiver clientReceiver = null;
    private String roomToCreate = null;
    private String roomToDelete = null;
    private boolean isBundleMsg = false;
    private ChatManager chatManager;
    private LocalPeerConnection localPeerConnection = null;
    private boolean runningInBackground = false;
    private String connectedServer;
    private String localPort;
    private boolean isMigrating;
    private boolean isListNeighborCalled = false;

    public ChatClient(ChatManager chatManager, String localServerHost, int iPort){
        this.localServerHost = localServerHost;
        this.chatManager = chatManager;
        this.iPort = iPort;
        this.gson = new Gson();
        localPort = "-1";
        isMigrating = false;
//        this.connectedServer = "self";
    }

    public void setMigrating(boolean migrating) throws IOException {
        isMigrating = migrating;
        if (!migrating){
            handle(true);
        }
    }

    public void setListNeighborCalled(boolean listNeighborCalled) {
        isListNeighborCalled = listNeighborCalled;
    }

    public boolean getListNeighborCalled() {
        return isListNeighborCalled;
    }

    public boolean isMigrating() {
        return isMigrating;
    }

    public String getConnectedServer() {
        return connectedServer;
    }

    public void setConnectedServer(String connectedServer) {
        this.connectedServer = connectedServer;
    }

    public boolean isRunningInBackground() {
        return runningInBackground;
    }

    public void setRunningInBackground(boolean runningInBackground) {
        this.runningInBackground = runningInBackground;
    }
    public String getLocalServerHost() {
        return localServerHost;
    }
    public LocalPeerConnection getLocalPeerConnection() {
        return localPeerConnection;
    }

    public boolean isConnectedLocally(){
        return localPeerConnection != null;
    }
    public void setQuitFlag(boolean quitFlag){
        this.quitFlag = quitFlag;
    }

    public boolean getQuitFlag(){
        return this.quitFlag;
    }

    public boolean isBundleMsg() {
        return isBundleMsg;
    }

    public void setBundleMsg(boolean bundleMsg) {
        isBundleMsg = bundleMsg;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public ClientReceiver getClientReceiver() {
        return clientReceiver;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    public Socket getSocket(){
        return this.socket;
    }

    public void setLocalPeerConnection(LocalPeerConnection localPeerConnection) {
        this.localPeerConnection = localPeerConnection;
    }

    public ClientSender getClientSender() {
        return clientSender;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    /** Connect to peer server:
     * remoteServerHost: 142.250.70,.238
     * remoteServerListeningPort: 4444
     * iPort is from java -jar chatpeer.jar [-p port] [-i port]
     * Step 1: create new socket with remote server's IP and port, plus options to specify TCP port
     * Step 2: issue HostChange command to remote peer server to inform it about current host's server IP and port
     * */
    public void makeConnection(String remoteServerHost, int specifiedLocalPort) throws IOException {
        // new connection request will be ignored if client is currently connected to a remote server
        if (!this.runningInBackground){
            System.out.println("connect to " + remoteServerHost);
        }
        quitFlag = false;
        if (remoteServerHost != null){
            String[] arrayList = remoteServerHost.split(":");
            String remoteServerIP = arrayList[0];
            int remoteServerPort;
            try {
                remoteServerPort  = Integer.parseInt(arrayList[1]);
            } catch (Exception e){

                System.out.println("Port should only contain numbers. Invalid port is given.");
                return;
            }
            if (localPeerConnection != null){
                chatManager.removeClientConnection(localPeerConnection);
                localPeerConnection = null;
            }


            if (specifiedLocalPort != -1){

                this.socket = new Socket(remoteServerIP, remoteServerPort, null,  specifiedLocalPort);
                this.socket.setReuseAddress(true);
            } else if (iPort != -1) {
                try{
                    this.socket = new Socket(remoteServerIP, remoteServerPort, null, iPort);
                    this.socket.setReuseAddress(true);
                }catch(Exception e) {
                    System.out.println("Connection to server " +  remoteServerPort +  " failed");
                    disconnect();
                    e.printStackTrace();
                    return;
                }

            } else {
                this.socket = new Socket(remoteServerIP, remoteServerPort);
                this.socket.setReuseAddress(true);
            }
            this.localPort = String.valueOf(socket.getLocalPort());
            this.remoteServerHost = remoteServerHost;

            this.writer = new PrintWriter(this.socket.getOutputStream(), true);
            ServerCommand hostChange = new HostChangeCommand(localServerHost);
            this.writer.println(gson.toJson(hostChange));
//            this.connectedServer = remoteServerHost;
            handle(false);
        }
    }



    public String getLocalPort() {
        return localPort;
    }

    public void printPrefix() {
        if (!this.runningInBackground){
            if (identity != null && !identity.equals("")){
                System.out.print("[" + roomid + "] " + identity + "> ");
            } else {
                System.out.print(">");
            }
        }
    }

    private void handle(boolean renew ) throws IOException {
        connected = true;
        try {
            if (!renew){
                if (!this.runningInBackground ){
                    this.clientSender = new ClientSender(socket, this);
                    clientSender.start();
                }

                clientReceiver = new ClientReceiver(this);
                clientReceiver.start();
            }

            while (connected && !runningInBackground && !isMigrating){
                Thread.sleep(2000);
            }

        } catch (InterruptedException e){
            System.out.println("Connection is interrupted");
            /** clientReceiver is a thread responsible for receiving message
             * clientSender is a thread responsible for sending message
             * */
            disconnect();
        }
        finally {
            if (socket != null && ! runningInBackground){
                System.out.println("Disconnected from localhost");
            }
            else if (!runningInBackground){
                System.out.println("Connection failed");
                socket.close();
            }

        }

    }

    public String getRemoteServerHost() {
        return remoteServerHost;
    }

    public void disconnect() throws IOException {
        if (clientReceiver != null){
            clientReceiver.setConnection_alive(false);
            clientReceiver.close();
            remoteServerHost = null;
        }
        if(clientSender != null){
            clientSender.close();
        }
        connected = false;
    }


    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getRoomid() {
        return roomid;
    }

    public void requestNewRoom(String roomid){
        this.roomToCreate = roomid;
    }

    public void processNewRoomResponse(){
        this.roomToCreate = null;
    }

    public String getRoomToCreate(){
        return this.roomToCreate;
    }


    public String getRoomToDelete(){
        return this.roomToDelete;
    }

    public void processRoomToDelete(){
        this.roomToDelete = null;
    }

    public void requestDeleteRoom(String roomid){
        this.roomToDelete = roomid;
    }
    public void handleKicked() throws IOException {
        this.quitFlag = true;
        try {
            this.setRoomid("");
            this.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}