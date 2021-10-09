package client;

import com.google.gson.Gson;
import server_command.HostChangeCommand;
import server_command.ServerCommand;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatClient {
    private Socket socket;
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

    public ChatClient(String localServerHost, int iPort){
        this.localServerHost = localServerHost;
        this.iPort = iPort;
        this.gson = new Gson();
    }

    public boolean isBundleMsg() {
        return isBundleMsg;
    }

    public void setBundleMsg(boolean bundleMsg) {
        isBundleMsg = bundleMsg;
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

    /** Connect to peer server:
     * remoteServerHost: 142.250.70,.238
     * remoteServerListeningPort: 4444
     * iPort is from java -jar chatpeer.jar [-p port] [-i port]
     * Step 1: create new socket with remote server's IP and port, plus options to specify TCP port
     * Step 2: issue HostChange command to remote peer server to inform it about current host's server IP and port
     * */
    public void makeConnection(String remoteServerHost, int specifiedLocalPort) throws IOException {
        // new connection request will be ignored if client is currently connected to a remote server
        if (remoteServerHost != null){
            String[] arrayList = remoteServerHost.split(":");
            String remoteServerIP = arrayList[0];
            int remoteServerPort = Integer.parseInt(arrayList[1]);
            System.out.println("remoteServerIP: " + remoteServerIP + " | remoteServerPort: " + remoteServerPort
                    + " | specifiedLocalPort: " + specifiedLocalPort + " | iPort: " + iPort +
                    " | localServerHost: " + localServerHost
            );

            if (specifiedLocalPort != -1){
                this.socket = new Socket(remoteServerIP, remoteServerPort, null,  specifiedLocalPort);
            } else if (iPort != -1) {
                this.socket = new Socket(remoteServerIP, remoteServerPort, null, iPort);
            } else {
                this.socket = new Socket(remoteServerIP, remoteServerPort);
            }
            this.remoteServerHost = remoteServerHost;

            this.writer = new PrintWriter(this.socket.getOutputStream(), true);
            ServerCommand hostChange = new HostChangeCommand(localServerHost);
            this.writer.println(gson.toJson(hostChange));
            handle();
        }
    }


    public void printPrefix() {
        if (remoteServerHost != null){
            System.out.print("[" + roomid + "] " + identity + "> ");
        }
    }

    private void handle() throws IOException {
        try {
            clientSender = new ClientSender(socket, this);
            clientReceiver = new ClientReceiver(this);

            clientSender.start();
            clientReceiver.start();

            while (connected){
                Thread.sleep(2000);
            }

        } catch (InterruptedException e){
            System.out.println("Connection is interrupted");
            /** clientReceiver is a thread responsible for receiving message
             * clientSender is a thread responsible for sending message
             * */
            clientReceiver.close();
            clientSender.close();
        } finally {
            if (socket != null){
                System.out.println("Disconnected from localhost");
                socket.close();
            }
        }

    }

    public void disconnect() {
        if (clientReceiver != null){
            clientReceiver.setConnection_alive(false);
            remoteServerHost = null;
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

}