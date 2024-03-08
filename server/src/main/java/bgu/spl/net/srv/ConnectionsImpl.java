package bgu.spl.net.srv;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl<T> implements Connections<T>{   // a kind of map of ConnectionHandler's, for example, it helps send() a number of ConnectionHandlers.

    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> connected_clients = new ConcurrentHashMap<Integer, ConnectionHandler<T>>(); // this holds the ConnectionHandlers of connected clients.


    public void connect(int connectionId, ConnectionHandler<T> handler){
        if(!(connected_clients.containsKey(connectionId))){
            connected_clients.put(connectionId, handler);
        }else{
            byte [] error=getErrPacket(7,"User already logged in Login username already connected.");  //  we need to make an error packet and send it to the client, and then disconnect him.
            this.send(connectionId,(T)error); //  send the error packet to the client,not sure if it true need to check it
        }
        //
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

        // do more stuff if needed, error handling etc.

    }




    public ConcurrentHashMap<Integer, ConnectionHandler<T>> getConnectedClients(){
        return connected_clients;
    }

    // public void setConnectedClients(ConcurrentHashMap<Integer, ConnectionHandler<T>> the_new){
    //     connected_clients = the_new;   //  not needed.
    // }



public byte[] getErrPacket(int errVal, String err_msg){
    byte[] op_byte = get2ByteArrFromShort((short)5);
    byte[] err_code_bytes = get2ByteArrFromShort((short)errVal);
    // byte[] err_code_bytes = new byte[2];
    // err_code_bytes[0] = (byte)0;
    // err_code_bytes[1] = (byte)errVal;
    byte[] concat1 = getCombinedByteArray(op_byte, err_code_bytes);  // concat so far, concat with more later

    byte[] err_msg_bytes = err_msg.getBytes();
    byte[] concat2 = getCombinedByteArray(concat1, err_msg_bytes);  // concat so far, concat with more later if needed more
    
    byte[] zeroByte = new byte[1];
    zeroByte[0] = (byte)0;
    byte[] concat3 = getCombinedByteArray(concat2, zeroByte);
    return concat3;
}
public short getShortFrom2ByteArr(byte[] byte2arr){
    return ((short)(((short)(byte2arr[0] & 0xFF)) << 8 | (short)(byte2arr[1] & 0xFF)));
}  //                                                                                       <<------------  conversions you gave us

public byte[] get2ByteArrFromShort(short shortNum){
    return (new byte[]{(byte)(shortNum >> 8), (byte)(shortNum & 0xff)});  // might be without the "="
}

public byte[] getCombinedByteArray(byte[] a, byte[] b){
    byte[] c = new byte[a.length + b.length];
    System.arraycopy(a, 0, c, 0, a.length);
    System.arraycopy(b, 0, c, a.length, b.length);
    return c;
} 
}   