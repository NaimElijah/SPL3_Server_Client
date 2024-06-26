package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl<T> implements Connections<T>{   // a kind of map of ConnectionHandler's, for example, it helps send() a number of ConnectionHandlers.

    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> connected_clients = new ConcurrentHashMap<Integer, ConnectionHandler<T>>(); // this holds the ConnectionHandlers of connected clients.
    private final Object FilesLock = new Object();


    public void connect(int connectionId, ConnectionHandler<T> handler){
        if(!(connected_clients.containsKey(connectionId))){
            connected_clients.put(connectionId, handler);
        }
    }



    public boolean send(int connectionId, T msg){   //  we need to send only to the clients that are logged in.
        if(connected_clients.containsKey(connectionId)){
            connected_clients.get(connectionId).send(msg);
            return true;
        }
        return false;

    }




    public void disconnect(int connectionId){
        ConnectionHandler<T> handler_removed = connected_clients.remove(connectionId);

        // handler_removed.setLoggedIn(false);  // already done in the close() below
        try { handler_removed.close(); } catch (IOException e) {}

    }




    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnectedClients(){
        return connected_clients;
    }


    public Object getFilesLock(){
        return this.FilesLock;
    }



}
