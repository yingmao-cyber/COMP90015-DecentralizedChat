package client_command;

import client.ChatClient;

public class MigrateRoomSuccessfulCommand extends ClientCommand{
    private final String type = "migrateroomsuccessfully";



    @Override
    public void execute(ChatClient chatClient) {
        chatClient.getChatManager().setCurrentMigrationSuccessful(true);
    }
}
