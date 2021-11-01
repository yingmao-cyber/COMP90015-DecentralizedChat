package client_command;

import client.ChatClient;

import java.util.List;

public class BlockingPeersCommand extends ClientCommand{
    private String type = "blockingpeers";
    private List<String> blockingIdentities;

    public BlockingPeersCommand(List<String> blockingIdentities) {
        this.blockingIdentities = blockingIdentities;
    }

    @Override
    public void execute(ChatClient chatClient) {
//        System.out.println("recv blocking peers command");
        chatClient.getChatManager().setRecvServerBlockingPeers(blockingIdentities);
        chatClient.getChatManager().setReceivedBlockingPeers(true);
    }
}
