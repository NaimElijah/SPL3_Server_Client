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

    public static void main(String[] args) throws IOException{

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



        // try {
        //     listeningThread.join();
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }








    }


}
