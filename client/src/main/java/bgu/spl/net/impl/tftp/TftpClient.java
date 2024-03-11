package bgu.spl.net.impl.tftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TftpClient {
    //TODO: implement the main logic of the client, when using a thread per client the main logic goes here
    // <<---------------------------  continue,  there needs to 2 threads, one thread reads from the keyboard and sends the command, and the other thread listens to the socket and handles
    //                                                                                                    what's been sent to the specific current client, he might send too(his response).
    public static void main(String[] args) throws IOException{  //TODO: take arguments from Maven, I think the ip and port numbers

        // ConcurrentLinkedQueue<byte[]> Commands_to_handle = new ConcurrentLinkedQueue<>(); - hedi said that we won't be tested for a client that sends another command when a command is in process
        boolean should_Terminate = false;
        TftpEncoderDecoder encdec = new TftpEncoderDecoder();
        Scanner s = new Scanner(System.in);
        Socket Server_socket = new Socket(args[0], 7777);

        if (args.length == 0){
            args = new String[]{"localhost", "7777"};
        }
        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }



        ListeningThread to_listen = new ListeningThread(Server_socket, args[0], 7777, encdec);
        Thread listeningThread = new Thread(to_listen, "Listening Thread");
        listeningThread.start();  //* starting to listen to the server   <<--------------------------------------


        try(Socket sock = Server_socket; // for automatic closing  // take argument 1 from maven
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))){

            while(!(should_Terminate)){  // while loop that constantly gets input from the user
                String UserInput = s.nextLine();  // keyboard thread waits for the next command to be typed by the client
                
                // encode it and send to the Server
                String[] UserInput_split = UserInput.split(" ");
                turn command to packet and send the packet to the server
                byte[] command_packet = new byte[0];  // concatenate to this the rest of the command according to what we got //TODO:  copy the helpful methods in the Tftp Protocol.

                if(UserInput_split[0] == "LOGRQ"){    //TODO:  maybe think about doing a keyboard thread class so that we can use the helpful methods. and move the things here to there.
                    //
                }else if(){
                    
                }

                out.write(command_packet);  //TODO: see how the write in connection handler does this
                out.newLine();
                out.flush();

                
                
            }

        }

        //TODO: Implement this Client !!










    }


}
