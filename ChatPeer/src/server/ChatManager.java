package server;

import client_command.RoomListCommand;

import java.util.*;
import java.util.logging.Logger;


/**
 * ChatManager class is responsible for performing admin tasks including:
 * managing clients (connections), joining room, changing room, etc.
 */

public class ChatManager {
    private HashMap<IConnection, String> clientConnectionList; // neighbour host list
    private HashMap<String, ArrayList<IConnection>> chatRooms;// room list
    private HashMap<String, IConnection> connectedClients;
    protected static String defaultRoomName = "";
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    private ChatServer chatServer;
    private ArrayList<String> recvNeighbors;
    private final List<RoomListCommand.RoomInfo> recvRoomInfo;
    private ArrayList<String> recvServerBlockingPeers;
    private volatile boolean isReceivedBlockingPeers = false;
    private volatile boolean isCurrentMigrationSuccessful;
    private HashSet<String> connectionFailedPeers;
    private volatile boolean connectionFailedAlert;


    public ChatManager(){
        clientConnectionList = new HashMap<>();
        chatRooms = new HashMap<>();
        chatRooms.put(defaultRoomName, new ArrayList<>());
        connectedClients = new HashMap<>();
        recvNeighbors = new ArrayList<>();
        recvRoomInfo = new ArrayList<>();
        recvServerBlockingPeers = new ArrayList<>();
        isCurrentMigrationSuccessful = false;
        connectionFailedPeers = new HashSet<>();
        connectionFailedAlert = false;
    }

    public boolean isReceivedBlockingPeers() {
        return isReceivedBlockingPeers;
    }

    public void setReceivedBlockingPeers(boolean receivedBlockingPeers) {
        isReceivedBlockingPeers = receivedBlockingPeers;
    }

    public void setConnectionFailedAlert(boolean connectionFailedAlert) {
        this.connectionFailedAlert = connectionFailedAlert;
    }

    public boolean isConnectionFailedAlert() {
        return connectionFailedAlert;
    }

    public void addConnectionFailedPeer(String peerAddress) {
        this.connectionFailedPeers.add(peerAddress);
    }

    public boolean isCurrentMigrationSuccessful() {
        return isCurrentMigrationSuccessful;
    }

    public void setCurrentMigrationSuccessful(boolean currentMigrationSuccessful) {
        isCurrentMigrationSuccessful = currentMigrationSuccessful;
    }

    public ArrayList<String> getRecvServerBlockingPeers() {
        return recvServerBlockingPeers;
    }

    public void setRecvServerBlockingPeers(List<String> recvServerBlockingPeers) {
        synchronized (this.recvServerBlockingPeers){
            this.recvServerBlockingPeers.addAll(recvServerBlockingPeers);
            this.recvServerBlockingPeers.notify();
        }
    }
    public void clearRecvServerBlockingPeers(){
        this.recvServerBlockingPeers.clear();
    }

    public List<RoomListCommand.RoomInfo> getRecvRoomInfo() {
        return recvRoomInfo;
    }

    public void setRecvRoomInfo(List<RoomListCommand.RoomInfo> recvRoomInfo) {
        synchronized (this.recvRoomInfo){
            this.recvRoomInfo.addAll(recvRoomInfo);
            this.recvRoomInfo.notify();
        }


    }

    public ArrayList<String> getRecvNeighbors() {
        return recvNeighbors;
    }

    public void setRecvNeighbors(ArrayList<String> recvNeighbors) {
        synchronized (this.recvNeighbors){
            if (this.recvNeighbors != null) {
                for (String s: recvNeighbors){
                    this.recvNeighbors.add(s);
                }
                this.recvNeighbors.notify();
            }
        }
    }
    public void clearRecvNeighbors(){
        this.recvNeighbors.clear();
    }

    public void clearRecvRoomInfos(){
        this.recvRoomInfo.clear();
    }

    public void setChatServer(ChatServer cs){
        this.chatServer = cs;
    }

    public synchronized HashMap<IConnection, String> getClientConnectionList() {return clientConnectionList;}

    public synchronized boolean isRoomIdExist(String roomid){
        return chatRooms.containsKey(roomid);
    }


    public synchronized void addClientToConnectionList(IConnection connection, String remotePeerHost){
        clientConnectionList.put(connection, remotePeerHost);
        connectedClients.put(connection.getName(),connection );
    }

    public boolean isClientInConnectionList(IConnection connection){
        return clientConnectionList.containsKey(connection);
    }

    public ArrayList<IConnection> getChatRooms(String roomid){
        return chatRooms.getOrDefault(roomid, new ArrayList<>());
    }

    public boolean hasChatRoom(String roomid){
        synchronized (chatRooms){
            if (chatRooms.getOrDefault(roomid, null) == null ){
                return false;
            }
            return true;
        }
    }

    public  void removeClientConnection(IConnection connection){
        synchronized (clientConnectionList){
            clientConnectionList.remove(connection);
        }
        synchronized (connectedClients){
            connectedClients.remove(connection.getName());
        }

        String roomName = connection.getCurrentChatRoom();
        // client leave room
        this.leaveRoom(connection, roomName);
    }

    public void broadCastAllRooms(String message, IConnection ignore){
        synchronized (this.chatRooms){
            for (ArrayList<IConnection> clients : this.chatRooms.values()){
                broadCastAGroup(clients,message, ignore );
            }
        }
    }

    public void broadCastAGroup(ArrayList<IConnection> clients, String message, IConnection ignore){
        synchronized (clients){
            for (IConnection s: clients){
                if (!s.equals(ignore)){
                    try{
                        s.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void broadCastToCurrentRoom(IConnection s, String message ,IConnection ignore){
        String roomName = s.getCurrentChatRoom();
//        LOGGER.info("Broadcast msg to " + roomName);
        ArrayList<IConnection> sc = this.chatRooms.get(roomName);
        if (sc != null){
            broadCastAGroup(sc, message,ignore);
        }
    }

    public void sendToOneClient(String message, IConnection connection){
//        LOGGER.info("Send msg to " +  serverConnection.getName());
        try{
            connection.sendMessage(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void leaveRoom(IConnection s, String roomid){
        synchronized (chatRooms){
//            System.out.println("Client " +  s.getName() + " leave the room " + roomid);
            ArrayList<IConnection> currentRoomClientList = this.chatRooms.get(roomid);
            if (currentRoomClientList != null){
                currentRoomClientList.remove(s);
                s.setCurrentChatRoom("");
            }
        }
    }

    public synchronized boolean joinRoom (IConnection s, String roomid){
        ArrayList<IConnection> newRoom = this.chatRooms.getOrDefault(roomid, null);
        // if the room to join exists
        if (newRoom != null){
//            LOGGER.info("Peer " +  s.getName() + " join the room " + roomid);
            String currentRoom = s.getCurrentChatRoom();
            ArrayList<IConnection> currentRoomClientList = this.chatRooms.get(currentRoom);
            if (currentRoomClientList != null) {
                currentRoomClientList.remove(s);
            }
            newRoom.add(s);
            return true;
        }
        return false;
    }


    public synchronized ArrayList<String> getRoomIdentities(String roomid){
//        LOGGER.info("Client requests " + roomid + " identities");
        ArrayList<String> identities = new ArrayList<>();
        ArrayList<IConnection> clientsInRoom = this.chatRooms.getOrDefault(roomid, null);
        if (clientsInRoom != null){
            for(IConnection s: clientsInRoom){
                identities.add(s.getName());
            }
        }
        return identities;
    }

    public HashMap<String, Integer> getRoomsInfo(String ignore, String addition){
        HashMap<String, Integer> roomsInfo = new HashMap<>();
        synchronized (this.chatRooms){
            for (Map.Entry<String, ArrayList<IConnection>> entry : this.chatRooms.entrySet()){
                String roomid = entry.getKey();
                if (!roomid.equals(ignore)){
                    roomsInfo.put(entry.getKey(), entry.getValue().size());
                }
            }
        }

        if (addition != null){
            roomsInfo.put(addition, 0);
        }

        return roomsInfo;
    }

    public boolean createRoom(String roomid){
        synchronized (chatRooms){
            if (isRoomIdValid(roomid) && !chatRooms.containsKey(roomid)){
                this.chatRooms.put(roomid, new ArrayList<>());
                return true;
            }
            else{
                return false;
            }
        }
    }

    private boolean isRoomIdValid(String roomid){
        boolean matchResult = roomid.matches("[A-Za-z0-9]+");
        boolean firstLetter = Character.isLetter(roomid.charAt(0));
        boolean lengthCheckResult = roomid.length() >= 3 && roomid.length() <= 32;
        return matchResult && firstLetter && lengthCheckResult;
    }

    public void removeRoom(String roomid){
        synchronized (chatRooms){
            if (chatRooms.containsKey(roomid)){
                chatRooms.remove(roomid);
            }
        }
    }

    public boolean  blockPeer(String peerId){
        IConnection blockClient = null;
        synchronized (connectedClients) {
            blockClient = this.connectedClients.get(peerId);
            if (blockClient != null){
                this.connectedClients.remove(peerId);
            }
        }
        if (blockClient != null){
//            LOGGER.info("Block peer " + peerId);
            this.removeClientConnection(blockClient);
            this.chatServer.blockClient(peerId);
            blockClient.close();
            return true;
        }
        return false;
    }
}
