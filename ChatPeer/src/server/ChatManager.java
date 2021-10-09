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
    protected static String defaultRoomName = "";
    public static final Logger LOGGER = Logger.getLogger(ChatServer.class.getName());


    public ChatManager(){
        clientConnectionList = new HashMap<>();
        chatRooms = new HashMap<>();
        chatRooms.put(defaultRoomName, new ArrayList<>());
    }

    public synchronized HashMap<ServerConnection, String> getClientConnectionList() {return clientConnectionList;}


    public void addClientToConnectionList(ServerConnection connection, String remotePeerHost){
        clientConnectionList.put(connection, remotePeerHost);
    }

    public boolean isClientInConnectionList(ServerConnection connection){
        return clientConnectionList.containsKey(connection);
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

        // client leave room
        this.leaveRoom(connection, roomName);


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
            LOGGER.info("Peer " +  s.getName() + " join the room " + roomid);
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


    public synchronized boolean createRoom(String roomid){
        synchronized (chatRooms){
            if (Validator.isRoomIdValid(roomid) && !chatRooms.containsKey(roomid)){
                this.chatRooms.put(roomid, new ArrayList<>());
                return true;
            }
            else{
                return false;
            }
        }
    }


    public boolean deleteRoom(ServerConnection s, String roomid){
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
            return true;
    }

}
