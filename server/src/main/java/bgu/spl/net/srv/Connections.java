package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {    // a kind of map of ConnectionHandler's, for example, it helps send() a number of ConnectionHandlers. <<====================  needs to be implemented.

    void connect(int connectionId, ConnectionHandler<T> handler);

    boolean send(int connectionId, T msg);

    void disconnect(int connectionId);
}
