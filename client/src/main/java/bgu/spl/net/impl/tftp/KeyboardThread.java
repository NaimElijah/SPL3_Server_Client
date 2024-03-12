package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KeyboardThread implements Runnable{

    //// private ConcurrentLinkedQueue<byte[]> Commands_to_handle;
    private Scanner s = new Scanner(System.in);
    private boolean should_Terminate = false;
    private Socket Servers_socket;
    private volatile String last_Command;  // if needed later, change this to a queue so mutable and shared with the Listening Thread.

    public KeyboardThread(Socket s, String l_c){
        this.Servers_socket = s;
        this.last_Command = l_c;
    }






    @Override
    public void run(){

        try(Socket sock = Servers_socket;  // for automatic closing
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))){ // maybe use  another buffered something

            while(!(should_Terminate)){  // while loop that constantly gets input from the user
                String UserInput = s.nextLine();  // keyboard thread waits for the next command to be typed by the client

                if(command_packet != null){
                
                    // encode it and send to the Server
                    String[] UserInput_split = UserInput.split(" ");

                    //TODO: turn command to packet     <<======================================

                    byte[] command_packet = new byte[0];  // concatenate to this the rest of the command according to what we got //TODO:  copy the helpful methods in the Tftp Protocol.

                    if(UserInput_split[0] == "LOGRQ"){    //TODO:  maybe think about doing a keyboard thread class so that we can use the helpful methods. and move the things here to there.
                        last_Command = UserInput_split[0];
                        //
                    }else if(){
                        last_Command = UserInput_split[0];
                        //
                    } more cases .....(more else ifs)....

                    if(command_packet != null){
                        out.write(command_packet);  //TODO: see how the write in connection handler does this,  // maybe use  another buffered something
                        out.newLine();
                        out.flush();
                    }

                
                
                }
            }
        }

    }



    
    
}
