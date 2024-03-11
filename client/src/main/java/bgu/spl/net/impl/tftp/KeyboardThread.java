package bgu.spl.net.impl.tftp;

import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KeyboardThread implements Runnable{

    // private ConcurrentLinkedQueue<byte[]> Commands_to_handle;
    // private Scanner s = new Scanner(System.in);
    // private boolean should_Terminate = false;

    public KeyboardThread(ConcurrentLinkedQueue<byte[]> Comm_to_ha){
        this.Commands_to_handle = Comm_to_ha;  //  and we'll add to that input_place
    }

    @Override
    public void run() {
        // // TODO: add the while loop that constantly gets input from the user
        // while(!(should_Terminate)){
        //     String UserInput = s.nextLine();  // keyboard thread waits for the next command to be typed by the client
        //     //TODO: encode it and send to the Server
        // }
    }
    
}
