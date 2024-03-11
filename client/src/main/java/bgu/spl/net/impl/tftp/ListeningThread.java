package bgu.spl.net.impl.tftp;

import java.net.Socket;

public class ListeningThread implements Runnable{

    private boolean should_terminate = false;
    private Socket Servers_Socket;
    private TftpEncoderDecoder Client_encdec;

    // more fields if needed

    public ListeningThread(Socket s, ){
        this.Servers_Socket = s;
        this. = ;
    }

    @Override
    public void run() {
        // TODO: a while loop where we listen to the socket for messages from the server.
        while (!(should_terminate)){       //  see if he got a packet from the server and if he got one he will Client_process it
            //
        }
    }


    public void or byte[] Client_process(byte[] message){
        // similar to the process of the Server, copy from there...
    }
    
}
