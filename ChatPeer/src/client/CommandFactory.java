package client;

import client_command.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import server_command.*;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class CommandFactory {
    private final Gson gson = new Gson();
    private ChatClient chatClient;
    private final ArrayList<String> commandRequiresNoInput = new ArrayList<>();

    public CommandFactory(ChatClient chatClient){
        this.commandRequiresNoInput.add("list");
        this.commandRequiresNoInput.add("quit");
        this.chatClient = chatClient;
    }

    /**
     * Message can be in two formats:
     * 1. "Hello World"
     * 2. Hello Word
     * if it is number 2, we need to join them together to produce a new string as input
     * @param inputArray
     * @return
     */
    private String joinMultipleArguments(ArrayList<String> inputArray){
        return String.join(" ", inputArray.subList(1, inputArray.size()));
    }

    /**
     * Covert user inputs to new command
     * Populate user input argument to initialize command object
     * @param userInput
     * @return
     */

    public ServerCommand convertUserInputToCommand(String userInput){
        String[] userInputs = userInput.split(" ");
        ArrayList<String> inputArray = new ArrayList<>();

        /** remove the empty spaces before the first input */
        for (String input: userInputs){
            if (input.length() > 0){
                inputArray.add(input);
            }
        }
        int inputLength = inputArray.size();

        if (inputLength != 0){

            /** if the command does not start with #, treat it as normal message */
            String prefix = inputArray.get(0).substring(0,1);
            String type = inputArray.get(0).substring(1);

            if (!prefix.equals("#")){
                return new MessageCommand(userInput);
            } else {
                if (!this.commandRequiresNoInput.contains(type) && inputLength == 1){
                    System.out.println("Command " + userInput + " is invalid.");
                    return null;
                }

            }

            String arg = "";
            if (inputLength > 1){
                arg = this.joinMultipleArguments(inputArray);
            }

            switch(type){
                case "identitychange":
                    return new IdentityChangeCommand(arg);
                case "join":
                    return new JoinCommand(arg);
                case "who":
                    return new WhoCommand(arg);
                case "list":
                    return new ListCommand();
                case "createroom":
                    // mark the client is requesting to create a new room
                    this.chatClient.requestNewRoom(arg);
                    return new CreateRoomCommand(arg);
                case "delete":
                    this.chatClient.requestDeleteRoom(arg);
                    return new DeleteCommand(arg);
                case "message":
                    return new MessageCommand(arg);
                case "quit":
                    return new QuitCommand();
                default:
                    System.out.println("Command " + userInput + " is invalid.");
                    return null;
            }
        }
        // if user doesn't input anything
        return new MessageCommand("");
    }

    private ClientCommand generateCommand(String jsonMessage, Class commandClass){
        return this.gson.fromJson(jsonMessage, (Type) commandClass);
    }

    /**
     * Covert json messages that are sent from server at client side to command object
     * @param jsonMessage
     * @return
     */
    public ClientCommand convertServerMessageToCommand(String jsonMessage){
        String type = gson.fromJson(jsonMessage, JsonObject.class).get("type").getAsString();

        switch(type){
            case "newidentity":
                return this.generateCommand(jsonMessage, NewIdentityCommand.class);
            case "message":
                return this.generateCommand(jsonMessage, MessageRelayCommand.class);
            case "roomchange":
                return this.generateCommand(jsonMessage, RoomChangeCommand.class);
            case "roomcontents":
                return this.generateCommand(jsonMessage, RoomContentsCommand.class);
            case "roomlist":
                return this.generateCommand(jsonMessage, RoomListCommand.class);
            default:
                return null;
        }
    }

}
