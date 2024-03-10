package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {    // a kind of map of ConnectionHandler's, for example, it helps send() a number of ConnectionHandlers.

    void connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);

    ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnectedClients();  // added by me

    Object getFilesLock();  // added by me

}
