package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ListeningThread implements Runnable{

    private boolean should_terminate = false;
    private Socket Servers_Socket;
    private TftpEncoderDecoder Client_encdec;
    private String hostArg0;

    // more fields if needed


    public ListeningThread(Socket s, String hostarg){
        this.Servers_Socket = s;
        this.hostArg0 = hostarg;
    }



    @Override
    public void run() {

        //BufferedReader and BufferedWriter automatically using UTF-8 encoding    <<-------  from echo, see if useful
        try(Socket sock = new Socket(hostArg0, 7777);  //   <<-------  from echo, see if useful
        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));  //   <<-------  from echo, see if useful
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))){  //   <<-------  from echo, see if useful
        //   <<-------  from echo, see if useful
        System.out.println("sending message to server");  //   <<-------  from echo, see if useful
        out.write(args[1]);  //   <<-------  from echo, see if useful
        out.newLine();  //   <<-------  from echo, see if useful
        out.flush();  //   <<-------  from echo, see if useful
            //   <<-------  from echo, see if useful
        System.out.println("awaiting response");  //   <<-------  from echo, see if useful
        String line = in.readLine();  //   <<-------  from echo, see if useful
        System.out.println("message from server: " + line);  //   <<-------  from echo, see if useful
        }  //   <<-------  from echo, see if useful



        // TODO: a while loop where we listen to the socket for messages from the server.
        while (!(should_terminate)){       //  see if he got a packet from the server and if he got one he will Client_process it
            //
        }
    }


    public void or byte[] Client_process(byte[] message){
        // similar to the process of the Server, copy from there...
    }
    
}
