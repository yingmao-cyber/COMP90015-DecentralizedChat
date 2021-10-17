package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import com.google.gson.Gson;
import server_command.ServerCommand;

class ClientSender extends Thread{
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
        this.writer = new PrintWriter(this.socket.getOutputStream(), true);
    }

    public void setConnection_alive(boolean connection_alive) {
        this.connection_alive = connection_alive;
    }

    public void close() {
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
                        } else {
                            this.writer.println(jsonMessage);
                        }
                    }
                    chatClient.printPrefix();
                }
            } catch (IOException e) {
                e.printStackTrace();
                connection_alive = false;
            } 
        }

        this.close();
    }
}