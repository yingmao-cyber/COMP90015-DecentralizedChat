package server_command;

import client_command.NewIdentityCommand;
import com.google.gson.Gson;
import server.ChatManager;
import server.ServerConnection;

public class IdentityChangeCommand extends ServerCommand {
    private String identity;
    private final String type = "identitychange";

    public IdentityChangeCommand(String identity){
        this.identity = identity;
    }

    /**
     * Requirements:
     * 1. must be an alphanumeric string: character and digits only
     * 2. must start with an upper or lower case character
     * 3. 3 <= length <=16
     * @return boolean
     */
    public static boolean isIdentityInvalid(String identity){
        boolean matchResult = identity.matches("[A-Za-z0-9]+"); // 1
        boolean firstCharacterCheck = Character.isLetter(identity.charAt(0)); // 2
        boolean lengthCheckResult = identity.length() >= 3 && identity.length() <= 16; // 3
        return !(matchResult && lengthCheckResult && firstCharacterCheck);
    }

    @Override
    public void execute(ServerConnection serverConnection){
        Gson gson = new Gson();
        String formerID = serverConnection.getName();
        String newID = identity;
        ChatManager chatManager = serverConnection.getChatManager();

        if (isIdentityInvalid(identity) || !chatManager.isUniqueIdentity(identity) ){
            newID = formerID;
        }

        serverConnection.setName(newID);
        NewIdentityCommand newIdentityCommand = new NewIdentityCommand(formerID, newID);

        /**
         * if identity does not change
         * - server responds NewIdentity message only to the client
         * if identity changed
         * - server responds NewIdentity message to all currently connected clients in the same room
         */
        String jsonMessage = gson.toJson(newIdentityCommand);
        System.out.println("Send: " + jsonMessage);

        if (newID.equals(formerID)){
            chatManager.sendToOneClient(jsonMessage, serverConnection);
        } else {
            chatManager.broadCastToCurrentRoom(serverConnection,jsonMessage, null );
        }

    }

}
