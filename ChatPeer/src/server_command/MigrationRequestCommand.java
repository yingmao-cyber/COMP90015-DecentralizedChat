package server_command;

import client_command.MigrateRoomSuccessfulCommand;
import com.google.gson.Gson;
import server.IConnection;


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
        System.out.println("Migrate room " + migrateRoomId + " created.");
        MigrateRoomSuccessfulCommand migrateRoomSuccessfulCommand = new MigrateRoomSuccessfulCommand();
        String jsonMessage = gson.toJson(migrateRoomSuccessfulCommand);
        connection.getChatManager().sendToOneClient(jsonMessage, connection);
    }
}
