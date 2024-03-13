package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KeyboardThread implements Runnable{

    private Scanner s = new Scanner(System.in);
    private boolean should_Terminate = false;
    private String ClientDir = ".\\";   //TODO:    remember to change it back to one \ for the linux assignment check, or use file.separator.

    private BufferedInputStream in;
    private BufferedOutputStream out;
    private Socket Servers_socket;

    private volatile String[] last_Command;  // to save a shared info of the last command for both threads.
    private volatile String[] curr_fileName;  // to save a shared info of the last command for both threads.

    public KeyboardThread(Socket s, String[] l_c, String[] f_n){
        this.Servers_socket = s;
        this.last_Command = l_c;
        this.curr_fileName = f_n;
    }






    @Override
    public void run(){

        try(Socket sock = Servers_socket){   // for automatic closing

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while(!(should_Terminate)){
                String UserInput = s.nextLine();  // keyboard thread waits for the next command to be typed by the client

                if(UserInput != null){
                    Send_to_Server(UserInput);
                }

            }

        }catch(IOException ex){
            ex.printStackTrace();
        }

    }





    






    private void Send_to_Server(String UserInput){

        String[] UserInput_split = UserInput.split(" ");

        System.out.println("UserInput_split[0] ----> " + UserInput_split[0]);  //! TEST !!!

        byte[] command_packet = new byte[2];  // build this command to be a packet according to what we got.


        //                                 //*  turning command to packet:
        if(UserInput_split[0].equals("LOGRQ")){
            last_Command[0] = UserInput_split[0];

            byte[] op_2bytes = get2ByteArrFromShort((short)7);
            byte[] username_bytes = UserInput_split[1].getBytes();
            byte[] concat1 = getCombinedByteArray(op_2bytes, username_bytes);

            byte[] zero_byte = new byte[1];
            zero_byte[0] = (byte)0;
            byte[] concat2 = getCombinedByteArray(concat1, zero_byte);
            
            command_packet = concat2;  // build done




        }else if(UserInput_split[0].equals("DELRQ")){
            last_Command[0] = UserInput_split[0];

            byte[] op_2bytes = get2ByteArrFromShort((short)8);
            byte[] fileName_bytes = UserInput_split[1].getBytes();
            byte[] concat1 = getCombinedByteArray(op_2bytes, fileName_bytes);

            byte[] zero_byte = new byte[1];
            zero_byte[0] = (byte)0;
            byte[] concat2 = getCombinedByteArray(concat1, zero_byte);
            
            command_packet = concat2;  // build done





        }else if(UserInput_split[0].equals("RRQ")){
            // check if the File exists
            File f = new File(ClientDir + UserInput_split[1]);
            boolean fExists = f.exists();

            if(fExists){
                // System.out.println(error message that the client already has this file); //TODO: see if we need to print in the assignment.
            }else{
                last_Command[0] = UserInput_split[0];

                byte[] op_2bytes = get2ByteArrFromShort((short)1);
                byte[] fileName_bytes = UserInput_split[1].getBytes();
                byte[] concat1 = getCombinedByteArray(op_2bytes, fileName_bytes);

                byte[] zero_byte = new byte[1];
                zero_byte[0] = (byte)0;
                byte[] concat2 = getCombinedByteArray(concat1, zero_byte);
                
                command_packet = concat2;  // build done
                this.curr_fileName[0] = UserInput_split[1];  // in case we'll need the file name
                
                try{
                    f.createNewFile();  // creating new file and the listening thread will add the data to it later after receiving all the data for it.            
                }catch(IOException e){
                    e.printStackTrace();
                }
        
            }





        }else if(UserInput_split[0].equals("WRQ")){
            // check if the File exists
            File f = new File(ClientDir + UserInput_split[1]);
            boolean fExists = f.exists();

            if(fExists){
                last_Command[0] = UserInput_split[0];
                this.curr_fileName[0] = UserInput_split[1]; // save the file name for later use.(in the Listening Thread). //TODO: see where else should I save the file name.

                byte[] op_2bytes = get2ByteArrFromShort((short)2);
                byte[] fileName_bytes = UserInput_split[1].getBytes();
                byte[] concat1 = getCombinedByteArray(op_2bytes, fileName_bytes);

                byte[] zero_byte = new byte[1];
                zero_byte[0] = (byte)0;
                byte[] concat2 = getCombinedByteArray(concat1, zero_byte);
                
                command_packet = concat2;  // build done
            }else{
                // don't build a command_packet and print:
                System.out.println("file does not exists");  // printing exactly what you wrote in the assignment to print.
            }







        }else if(UserInput_split[0].equals("DIRQ")){
            last_Command[0] = UserInput_split[0];
            
            byte[] op_2bytes = get2ByteArrFromShort((short)6);
            command_packet = op_2bytes;  // build done



        }else if(UserInput_split[0].equals("DISC")){
            last_Command[0] = UserInput_split[0];

            byte[] op_2bytes = get2ByteArrFromShort((short)10);
            command_packet = op_2bytes;  // build done

        }





        if((command_packet != null) && (command_packet.length > 0)){     //  about to send to the Server.
                try{
                    for(byte b : command_packet){
                        System.out.println(b);  //!  TESTING !!
                    }
                    // System.out.println(command_packet.toString());  //!  TESTING !!
                    System.out.println("current command length -----> " + command_packet.length);  //!  TESTING !!
                    out.write(command_packet);
                    out.flush();
                }catch(IOException e){
                    e.printStackTrace();
                }
        }



    }  //*  END OF Send_to_Server.















    public byte[] getCombinedByteArray(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public short getShortFrom2ByteArr(byte[] byte2arr){
        // return (short)(((short) byte2arr[0]) << 8 | (short) (byte2arr[1]) & 0x00ff);    // hedi's 
        return ((short)(((short)(byte2arr[0] & 0xFF)) << 8 | (short)(byte2arr[1] & 0xFF)));
    }  //                                                                                       <<------------  conversions you gave us

    public byte[] get2ByteArrFromShort(short shortNum){
        return (new byte[]{(byte)(shortNum >> 8), (byte)(shortNum & 0xff)});  // might be without the "="
    }

    
    
}
