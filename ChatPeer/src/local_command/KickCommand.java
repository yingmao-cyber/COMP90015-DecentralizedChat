package local_command;

import client.ChatClient;
import server.ChatManager;

public class KickCommand extends LocalCommand{
    private String peerId;
    private final String type = "kick";

    public KickCommand(String peerId) {
        this.peerId = peerId;
    }

    @Override
    public void execute(ChatClient chatClient, ChatManager chatManager) {
        boolean result = chatManager.blockPeer(this.peerId);
        if (result) {
            System.out.println("Peer " + peerId + " is blocked.");
        } else {
            System.out.println("Peer " + peerId + "block failed");
        }
    }
}
