package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import com.google.gson.Gson;
import local_command.MigrateRoomCommand;
import local_command.SearchCommand;
import server_command.ServerCommand;

public class ClientSender extends Thread{
    private Socket socket;
    private ChatClient chatClient;
    private PrintWriter writer;
    private BufferedReader userInput;
    private Gson gson;
    private boolean connection_alive;
    private CommandFactory commandFactory;

    public ClientSender(Socket socket, ChatClient chatClient) throws IOException {
        // Client must know the hostname or IP of teh machine and on which the server is running
        this.socket = socket;
        this.connection_alive = true;
        this.gson = new Gson();
        this.chatClient = chatClient;
        this.commandFactory = new CommandFactory(this.chatClient);
        this.userInput = new BufferedReader(new InputStreamReader(System.in));

        // autoFlush = true means send the data immediately when receiving the input
//        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
        this.writer = chatClient.getWriter();
    }

    public void setConnection_alive(boolean connection_alive) {
        this.connection_alive = connection_alive;
    }

    public void close(){
        this.connection_alive = false;
    }

    /**
     * Send message to server
     */
    public void run() {
        while (connection_alive) {
            try {
                String str = userInput.readLine();
                if (str != null){
                    ServerCommand command = commandFactory.convertUserInputToCommand(str);
                    if (command != null){
                        String jsonMessage = gson.toJson(command);
                        // convert user input to command and convert command to json object
                        // then send this json command object to server
                        if (str.equals("#quit")){
                            chatClient.setQuitFlag(true);
                            this.writer.println(jsonMessage);
                            connection_alive = false;
                        }
                        else {
                            this.writer.println(jsonMessage);
                        }
                    }
                    else if (str.equals("#searchnetwork")){
                        SearchCommand s = new SearchCommand();
                        s.execute(chatClient, chatClient.getChatManager());
                    }
                    else{
                        String[] userInputs = str.split(" ");
                        ArrayList<String> inputArray = new ArrayList<>();
                        /** remove the empty spaces before the first input */
                        for (String input: userInputs){
                            if (input.length() > 0){
                                inputArray.add(input);
                            }
                        }
                        int inputLength = inputArray.size();
                        if (inputArray.get(0).equals("#migrateroom")){
                            if(inputLength != 2){
                                System.out.println("Command " + userInput + " is invalid!!");
                            }else{
                                MigrateRoomCommand migrateRoomCommand = new MigrateRoomCommand(inputArray.get(1));
                                migrateRoomCommand.execute(chatClient,chatClient.getChatManager());
                            }
                        }

                    }
                    chatClient.printPrefix();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                connection_alive = false;
            } 
        }

        this.close();
    }
}