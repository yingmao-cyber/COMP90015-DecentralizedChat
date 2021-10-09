package server;

import com.google.gson.Gson;
import server_command.JoinCommand;
import shared.Validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;


/**
 * ChatManager class is responsible for performing admin tasks including:
 * managing clients (connections), joining room, changing room, etc.
 */

public class ChatManager {
    private HashMap<ServerConnection, String> clientConnectionList;
    private HashMap<String, ArrayList<ServerConnection>> chatRooms;// room list
    private HashMap<String, ServerConnection> roomOwnership;
    protected static String defaultRoomName = "MainHall";
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());
    private final Gson gson;


    public ChatManager(){
//        this.createDefaultRoom();
        clientConnectionList = new HashMap<>();
        chatRooms = new HashMap<>();
        roomOwnership= new HashMap<>();
        chatRooms.put(defaultRoomName, new ArrayList<>());
        roomOwnership.put(defaultRoomName, null); // main hall does not have owner
        gson =  new Gson();
    }

    public synchronized HashMap<ServerConnection, String> getClientConnectionList() {return clientConnectionList;}


    public void addClientToConnectionList(ServerConnection connection, String remotePeerHost){
        clientConnectionList.put(connection, remotePeerHost);
    }

    public boolean isClientInConnectionList(ServerConnection connection){
        return clientConnectionList.containsKey(connection);
    }

    public void addClientConnection(ServerConnection connection, String jsonMessage){
        // place client to default room
        LOGGER.info("New user:  " + connection.getName());

        this.sendToOneClient(jsonMessage, connection);

        JoinCommand j = new JoinCommand(defaultRoomName);
        j.execute(connection);

    }

    public void removeEmptyRoomWithOwnerDropped(){
        ArrayList<String> roomsToBeRemoved = new ArrayList<>();
        synchronized (this.roomOwnership) {
            for (Map.Entry<String, ServerConnection> roomOwner : this.roomOwnership.entrySet()) {
                ServerConnection roomValue = roomOwner.getValue();
                String roomKey = roomOwner.getKey();
                if (getRoomSize(roomKey) == 0 && !roomKey.equals(ChatManager.defaultRoomName) && roomValue == null){
                    LOGGER.info("Remove room " +  roomKey);
                    synchronized (this.chatRooms){
                        this.chatRooms.remove(roomKey); // remove room
                    }
                    roomsToBeRemoved.add(roomKey);
                }
            }
            for (String room: roomsToBeRemoved){
                this.roomOwnership.remove(room); // remove the ownership since the room is removed
            }
        }
    }

    public void removeClientConnection(ServerConnection connection){
        synchronized (clientConnectionList){
            clientConnectionList.remove(connection);
        }

        String roomName = connection.getCurrentChatRoom();
        LOGGER.info("Remove " + connection.getName() + " from " + roomName);

        synchronized (this.chatRooms){
            ArrayList<ServerConnection> currentRoomClientList = this.chatRooms.get(roomName);
            if (currentRoomClientList != null){
                currentRoomClientList.remove(roomName);
            }
        }

        // if the room creator quit, set room owner to null for all rooms that owner owned
        synchronized (this.roomOwnership) {
            for (Map.Entry<String, ServerConnection> roomOwner : this.roomOwnership.entrySet()) {
                ServerConnection room = roomOwner.getValue();
                if (room != null){
                    if (room.equals(connection)) {
                        roomOwner.setValue(null);
                    }
                }
            }
        }

        // get room owner
        ServerConnection currentOwner;
        synchronized (this.roomOwnership){
            currentOwner = this.roomOwnership.get(roomName);
        }

        // client leave room
        this.leaveRoom(connection, roomName);

        //discard the room when the following conditions applied:
        removeEmptyRoomWithOwnerDropped();

    }

    public void broadCastAllRooms(String message, ServerConnection ignore){
        for (ArrayList<ServerConnection> clients : this.chatRooms.values()){
            synchronized (clients){
                broadCastAGroup(clients,message, ignore );
            }
        }
    }

    public void broadCastAGroup(ArrayList<ServerConnection> clients, String message, ServerConnection ignore){
        synchronized (clients){
            for (ServerConnection s: clients){
                if (ignore == null || !s.equals(ignore)){
                    try{
                        s.sendMessage(message);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void broadCastToCurrentRoom(ServerConnection s, String message ,ServerConnection ignore){
        String roomName = s.getCurrentChatRoom();
        LOGGER.info("Broadcast msg to " + roomName);
        ArrayList<ServerConnection> sc = this.chatRooms.get(roomName);
        if (sc != null){
            broadCastAGroup(sc, message,ignore);
        }
    }

    public void sendToOneClient(String message, ServerConnection serverConnection){
//        LOGGER.info("Send msg to " +  serverConnection.getName());
        try{
            serverConnection.sendMessage(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public synchronized boolean isUniqueIdentity(String identity){
        for (ArrayList<ServerConnection> clients: this.chatRooms.values()){
            for (ServerConnection s : clients){
                if (identity.equals(s.getName())){
                    return false;
                }
            }
        }
        return true;
    }


    public synchronized void leaveRoom(ServerConnection s, String roomid){
        // if the room to join exists
        System.out.println("Client " +  s.getName() + " leave the room " + roomid);
        String currentRoom = s.getCurrentChatRoom();
        ArrayList<ServerConnection> currentRoomClientList = this.chatRooms.get(currentRoom);
        if (currentRoomClientList != null){
            currentRoomClientList.remove(s);
            s.setCurrentChatRoom("");
        }
    }

    public synchronized boolean joinRoom (ServerConnection s, String roomid){
        ArrayList<ServerConnection> newRoom = this.chatRooms.getOrDefault(roomid, null);
        // if the room to join exists
        if (newRoom != null){
            LOGGER.info("Client " +  s.getName() + " join the room " + roomid);
            String currentRoom = s.getCurrentChatRoom();
            ArrayList<ServerConnection> currentRoomClientList = this.chatRooms.get(currentRoom);
            if (currentRoomClientList != null) {
                currentRoomClientList.remove(s);
            }
            newRoom.add(s);
            return true;
        }
        return false;
    }

    public synchronized int getRoomSize(String roomid){
        return this.chatRooms.get(roomid).size();
    }

    public String getRoomOwner(String roomid){
        ServerConnection owner;
        synchronized(this.roomOwnership) {
            owner = this.roomOwnership.getOrDefault(roomid, null);
        }
        return owner == null? "" : owner.getName();
    }

    public synchronized ArrayList<String> getRoomIdentities(String roomid){
//        LOGGER.info("Client requests " + roomid + " identities");
        ArrayList<String> identities = new ArrayList<>();
        ArrayList<ServerConnection> clientsInRoom = this.chatRooms.getOrDefault(roomid, null);
        if (clientsInRoom != null){
            for(ServerConnection s: clientsInRoom){
                identities.add(s.getName());
            }
        }
        return identities;
    }

    public HashMap<String, Integer> getRoomsInfo(String ignore, String addition){
        HashMap<String, Integer> roomsInfo = new HashMap<>();
        synchronized (this.chatRooms){
            for (Map.Entry<String, ArrayList<ServerConnection>> entry : this.chatRooms.entrySet()){
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


    public synchronized boolean createRoom(ServerConnection s, String roomid){

        if (Validator.isRoomIdValid(roomid) && !this.roomOwnership.containsKey(roomid)){
            this.chatRooms.put(roomid, new ArrayList<>());
            this.roomOwnership.put(roomid, s);
            return true;
        }
        else{
            return false;
        }

    }

    //check if the room is valid and the owner is the input identity
    private synchronized boolean isRoomOwner (ServerConnection identity, String roomid){
        ServerConnection owner = this.roomOwnership.get(roomid);
        if (owner == null){
            return false;
        }else{
            return owner.getName().equals(identity.getName());
        }
    }

    public boolean deleteRoom(ServerConnection s, String roomid){
        if (isRoomOwner(s, roomid)){
            ArrayList<ServerConnection> clientsInRoom;
            synchronized (this.chatRooms) {
                clientsInRoom = this.chatRooms.get(roomid);
            }
                if (clientsInRoom == null ){
                    System.out.println("Delete fail because no such room exist ");
                    return false;
                }
                // in order to prevent concurrency change to the array, make a shallow copy
            ArrayList<ServerConnection> copiedClients = new ArrayList<ServerConnection>(clientsInRoom);
            for(ServerConnection connection: copiedClients){
                if (!connection.equals(s)){
                    JoinCommand j = new JoinCommand(defaultRoomName);
                    j.execute(connection);
                }
            }

            synchronized (this.chatRooms) {
                this.chatRooms.remove(roomid);
            }
            synchronized (this.roomOwnership){
                this.roomOwnership.remove(roomid);
            }
            return true;
        }
        System.out.println("Delete fail because you are not an owner ");
        return false;
    }

}
