package client;

import local_command.LocalCommand;
import local_command.LocalCommandFactory;
import server.ChatManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LocalCommandHandler extends Thread {
    private boolean connected = true;
    private final ChatManager chatManager;
    private final ChatClient chatClient;
    private BufferedReader userInput;
    private LocalCommandFactory commandFactory;

    public LocalCommandHandler(ChatClient chatClient, ChatManager chatManager){
        this.chatManager = chatManager;
        this.chatClient = chatClient;
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
        this.commandFactory = new LocalCommandFactory(chatClient);
    }

    public void run(){
        connected = true;
        while (connected) {
            try {
                System.out.print(">");
                String str = userInput.readLine();
                if (str != null){
                    LocalCommand command = commandFactory.convertUserInputToCommand(str);
                    if (command != null){
                        command.execute(chatClient, chatManager);
                    }
                }
            } catch (IOException e) {
                connected = false;
                e.printStackTrace();
            }
        }
    }
}
