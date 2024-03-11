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

        ConcurrentLinkedQueue<byte[]> Commands_to_handle = new ConcurrentLinkedQueue<>();
        boolean should_Terminate = false;
        Scanner s = new Scanner(System.in);

        if (args.length == 0){
            args = new String[]{"localhost", "7777"};
        }

        if (args.length < 2) {
            System.out.println("you must supply two arguments: host, port");
            System.exit(1);
        }

        //



        ListeningThread to_listen = new Lis
        Thread listeningThread = new Thread(to_listen, "Listening Thread");
        listeningThread.start();

        // TODO: add the while loop that constantly gets input from the user
        while(!(should_Terminate)){
            String UserInput = s.nextLine();  // keyboard thread waits for the next command to be typed by the client
            //TODO: encode it and send to the Server
        }



















        if (args.length == 0) {  // <<-----  taken from echo
            args = new String[]{"localhost", "hello"};  // <<-----  taken from echo
        }  // <<-----  taken from echo

        if (args.length < 2) {  // <<-----  taken from echo
            System.out.println("you must supply two arguments: host, message");  // <<-----  taken from echo
            System.exit(1);  // <<-----  taken from echo
        }  // <<-----  taken from echo

        //BufferedReader and BufferedWriter automatically using UTF-8 encoding
        try (Socket sock = new Socket(args[0], 7777);
                BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()))){

            System.out.println("sending message to server");
            out.write(args[1]);
            out.newLine();
            out.flush();

            System.out.println("awaiting response");
            String line = in.readLine();
            System.out.println("message from server: " + line);
        }



        System.out.println("implement me!");
        // <<---------------------------  continue

    }
}
