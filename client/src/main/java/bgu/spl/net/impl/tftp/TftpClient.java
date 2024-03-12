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
    // <<---------------------------  continue,  there needs to 2 threads, one thread reads from the keyboard and sends the command, and the other thread listens to the socket and handles
    //                                                                                                    what's been sent to the specific current client, he might send too(his response).
    public static void main(String[] args) throws IOException{

        // boolean should_Terminate = false;  // see if a boolean like this is needed here
        TftpEncoderDecoder encdec = new TftpEncoderDecoder();

        if (args.length == 0){
            args = new String[]{"localhost", "7777"};
        }
        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }

        Socket Servers_socket = new Socket(); // just for initialization
        try{
            Servers_socket = new Socket(args[0], Integer.parseInt(args[1]));  //  creating the socket of the server that we'll READ FROM && WRITE TO.
        }catch(NumberFormatException e){
            e.printStackTrace();
        }


        ListeningThread to_listen = new ListeningThread(Servers_socket, encdec);
        Thread listeningThread = new Thread(to_listen, "Listening Thread");

        listeningThread.start();  //* starting the Listening Thread   <<-------------------
        
        // Keyboard Thread is started by the Listening Thread

        //? maybe we'll need a while loop here to keep it running   <<--------------------------------------- or maybe just use .join()
        try {
            listeningThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }








    }


}
