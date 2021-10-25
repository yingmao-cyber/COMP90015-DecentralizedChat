package local_command;

import client.ChatClient;

import java.util.ArrayList;

public class LocalCommandFactory {
    private ChatClient chatClient;
    private ArrayList<String> commandWithOneArg = new ArrayList<>();
    private ArrayList<String> commandWithZeroArg = new ArrayList<>();

    public LocalCommandFactory(ChatClient chatClient){
        this.chatClient = chatClient;
        commandWithOneArg.add("connect");
        commandWithOneArg.add("delete");
        commandWithOneArg.add("createroom");
        commandWithOneArg.add("kick");
        commandWithZeroArg.add("searchnetwork");
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
            String prefix;
            String type;
            String argOne = "";


            /** if the command does not start with #, treat it as normal message */
            try {
                prefix = inputArray.get(0).substring(0,1);
                type = inputArray.get(0).substring(1);
                if (!prefix.equals("#")){
                    return null;
                }
                if (commandWithZeroArg.contains(type)){
                    argOne = "";
                }
                else if (commandWithOneArg.contains(type)){
                    argOne = inputArray.get(1);
                }
            } catch (Exception e){
                System.out.println("Command " + userInput + " is invalid.");
                return null;
            }

            switch(type){
                case "connect":
                    String remoteServerHost = argOne;
                    int specifiedLocalPort = -1;
                    if (inputLength > 2){
                        try {
                            specifiedLocalPort = Integer.parseInt(inputArray.get(2));
                        } catch (Exception e){
                            System.out.println("Port should only contain numbers. Invalid port is given.");
                            return null;
                        }

                    }
                    return new ConnectCommand(remoteServerHost, specifiedLocalPort );
                case "createroom":
                    return new CreateRoomCommand(argOne);
                case "delete":
                    return new DeleteCommand(argOne);
                case "searchnetwork":
                    return new SearchCommand();
                case "kick":
                    if (inputArray.size() == 1){
                        System.out.println("Command " + userInput + " is invalid.");
                        chatClient.printPrefix();
                        return null;
                    }
                    return new KickCommand(argOne);
                default:
                    return null;
            }
        }
        // if user doesn't input anything
        return null;
    }

}
