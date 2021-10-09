package local_command;

import client.ChatClient;

import java.util.ArrayList;

public class LocalCommandFactory {
    private ChatClient chatClient;

    public LocalCommandFactory(ChatClient chatClient){
        this.chatClient = chatClient;
    }

    public LocalCommand convertUserInputToCommand(String userInput){
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
                return null;
            }

            switch(type){
                case "connect":
                    String remoteServerHost = inputArray.get(1);
                    int specifiedLocalPort = -1;
                    if (inputLength > 2){
                        specifiedLocalPort = Integer.parseInt(inputArray.get(2));
                    }
                    return new ConnectCommand(remoteServerHost, specifiedLocalPort );
                case "createroom":
                    return new CreateRoomCommand(inputArray.get(1));
                case "delete":
                    System.out.println("delete called " + inputArray.get(1));
                    return new DeleteCommand(inputArray.get(1));
                default:
                    System.out.println("Command " + userInput + " is invalid.");
                    return null;
            }
        }
        // if user doesn't input anything
        return null;
    }

}
