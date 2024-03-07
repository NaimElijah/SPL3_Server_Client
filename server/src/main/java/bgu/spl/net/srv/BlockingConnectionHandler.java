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
        try (Socket sock = this.sock) {  // just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0){
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null){
                    protocol.process(nextMessage);
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }






    @Override
    public void send(T msg) {  // send back to the client  <<-----------------------

        try {
            if (msg != null) {
                out.write(encdec.encode(msg));  // we might need to send in a couple of packages if it exceeds 512 bytes. mybe use a for loop for this
                out.flush();   // hedi said that there might be a bug here, that there might need to be a wider check/oversee here, and that 'out' might not have been initialized yet in run(),
            }                                     //   but can be fixed by ourselves(right order of things).    *I don't think that will be a problem, the run() will be executed already.*
        } catch (IOException ex) {                // **he afterwards said that the bug might be that if 2 clients send together, the packets they send might intertwine, so check that, resolve.
            ex.printStackTrace();                 // **we don't want 2 packets to mix with each other, see that that doesn't happen. We need to do something in this method to take care of this.
        }                                         //                                                                           **using synchronization or a data structure or ....
        //                                                                                     * he said the sync is per client.(because every client has his seperate handler's thread).

        //TODO: IMPLEMENT IF NEEDED           <<-----------------------------  * THIS WILL BE NEEDED !! *
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
