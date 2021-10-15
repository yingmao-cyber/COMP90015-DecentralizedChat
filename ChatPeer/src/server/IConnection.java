package server;

import java.io.IOException;

public interface IConnection {
    void setCurrentChatRoom(String currentChatRoom);
    void setName(String identity);
    String getName();
    String getCurrentChatRoom();
    void sendMessage(String message)  throws IOException;
    void close();
    ChatManager getChatManager();
    String getConnType();

}
