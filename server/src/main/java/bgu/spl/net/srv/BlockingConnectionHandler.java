package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.BidiMessagingProtocol;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    
    private volatile boolean logged_in = false;  // added by me


    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    }



    @Override
    public void run(){                    //   ====================>>    This is the main while loop of the runnable(here it's the thread)    <<====================
        try(Socket sock = this.sock){  // just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0){
                T nextMessage = encdec.decodeNextByte((byte)read);
                if (nextMessage != null){
                    protocol.process(nextMessage);
                }
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }

    }





    @Override
    public void send(T msg) {  // send to the client  <<-----------------------

        try{
            if(msg != null){
                out.write(encdec.encode(msg));
                out.flush();
            }
        }catch (IOException ex) {
            ex.printStackTrace();
        }

    }








    public BidiMessagingProtocol<T> getProtocol(){
        return protocol;
    }



    public boolean getLoggedIn(){
        return logged_in;
    }

    public void setLoggedIn(boolean bool){
        logged_in = bool;
    }


    @Override
    public void close() throws IOException{
        connected = false;
        logged_in = false;
        sock.close();
    }


}
