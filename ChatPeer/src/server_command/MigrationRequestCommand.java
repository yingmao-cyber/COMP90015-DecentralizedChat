package server_command;

import client_command.MigrateRoomSuccessfulCommand;
import client_command.RoomListCommand;
import com.google.gson.Gson;
import server.IConnection;

import java.util.List;

public class MigrationRequestCommand extends ServerCommand{
    private String  migrateRoomId;
    private final String type = "migrationrequest";

    public MigrationRequestCommand(String migrateRoomId) {
        this.migrateRoomId = migrateRoomId;
    }

    @Override
    public void execute(IConnection connection) {
        System.out.println("Receive migration request");
        Gson gson = new Gson();
        connection.getChatManager().createRoom(migrateRoomId);
        MigrateRoomSuccessfulCommand migrateRoomSuccessfulCommand = new MigrateRoomSuccessfulCommand();
        String jsonMessage = gson.toJson(migrateRoomSuccessfulCommand);
        connection.getChatManager().sendToOneClient(jsonMessage, connection);
    }
}
