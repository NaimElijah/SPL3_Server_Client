package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ListeningThread implements Runnable{

    private boolean should_terminate = false;
    private Socket Servers_Socket;
    private TftpEncoderDecoder encdec;
    private String hostArg0;
    private int port;

    // more fields if needed

    public ListeningThread(Socket s, String hostarg, int portt, TftpEncoderDecoder ende){
        this.Servers_Socket = s;
        this.hostArg0 = hostarg;
        this.port = portt;
        this.encdec = ende;
    }



    @Override
    public void run() {

        try(Socket sock = Servers_Socket;   //  for automatic closing
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))){

            // while loop where we listen to the socket for messages from the server.
            while (!(should_terminate)){       //  see if he got a packet from the server and if he got one he will Client_process it
                // get a message/packet from the server, this will probably wait for a message

                out.write(Client_process(the message/packet from the Server));
                out.newLine();
                out.flush();
            }

        }  //   <<-------  from echo, see if useful
    }


    public byte[] Client_process(byte[] message){
        // similar to the process of the Server, copy from there...
    }
    
}
