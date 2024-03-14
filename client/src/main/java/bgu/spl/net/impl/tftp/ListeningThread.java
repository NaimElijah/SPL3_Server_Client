package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ListeningThread implements Runnable{

    private boolean should_terminate = false;
    private boolean should_write_back = false;
    private boolean is_first = true;  // for WRQ in the Client_process

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private Socket Servers_Socket;
    private TftpEncoderDecoder encdec;
    private Thread keyboardThread;

    private volatile String[] last_Command;  // to save a shared info of the last command for both threads.
    private volatile String[] curr_fileName;  // to save a shared info of the last command for both threads.

    private String ClientDir = "." + File.separator;   //TODO:    remember to change it back to one \ for the linux assignment check.

    private byte[] file_bytes; // byte BUFFER WHEN CLIENT READS FROM HIS Files
    private ConcurrentLinkedQueue<byte[]> DATA_parts_to_Send = new ConcurrentLinkedQueue<byte[]>();  //? <<---------------- for WRITING to the server.
    private short Block_Number_Count = 0;
    // * send 512(518 with others) data max every time and the receiver keeps receiving until he gets something that isn't full, that isn't 512(518) bytes.

    private byte[] Data_ReceivedTillNow_Buffer = new byte[0];  //? <<---------------- for READING from the server.
    // * keep all the bytes received in the buffer until we get something that isn't full, that isn't 512(518) bytes, add it as well and then write it all into the Files of the Client.


    public ListeningThread(Socket s, TftpEncoderDecoder ende){
        this.Servers_Socket = s;
        this.encdec = ende;
        this.last_Command = new String[1];
        this.curr_fileName = new String[1];
    }





    @Override
    public void run(){
        
        KeyboardThread to_write = new KeyboardThread(Servers_Socket, this.last_Command, this.curr_fileName);  // remember this is a runnable object.
        keyboardThread = new Thread(to_write, "Keyboard Thread");  // this is the Keyboard Thread.
        keyboardThread.start();  //   <<------------------   and this is the Keyboard Thread's start up.

        try(Socket sock = this.Servers_Socket){  // for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while ((!(should_terminate)) && ((read = in.read()) >= 0)){
                byte[] nextMessage = encdec.decodeNextByte((byte)read);
                if (nextMessage != null){
                    Client_process(nextMessage);
                }
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }

        // keyboardThread.interrupt();  //  if we got a ACK after we made a DISC we'll change the should_terminate to true   // already stops himself when getting a DISC.

        try{
            this.Servers_Socket.close();  // close the socket.
        }catch(IOException e){
            e.printStackTrace();
        }

    }









    public void Client_process(byte[] message){

        byte[] opc2BytesArr = new byte[2];
        opc2BytesArr[0] = message[0];
        opc2BytesArr[1] = message[1];
        short opcode = getShortFrom2ByteArr(opc2BytesArr);  // short opcode

        byte[] message_back_to_Server = new byte[0];  // just for initialization, set it up according to what we want to send back to the server if want to send something back to the Server.


        // System.out.println("current Packet received in Client is below:");  //!  TESTING !!
        // for(byte b : message){
        //     System.out.print(b + ",");  //!  TESTING !!
        // }
        // System.out.println("end of the current Packet received in Client");  //!  TESTING !!










        if(opcode == 3){   //*  got a DATA Packet from the Server  

            // get the DATA byte[]:

            byte[] the_data = Arrays.copyOfRange(message, 6, message.length);

            if(the_data.length >= 512){
                // we got a full DATA Packet, add the DATA byte[] to the buffer field
                Data_ReceivedTillNow_Buffer = getCombinedByteArray(Data_ReceivedTillNow_Buffer, the_data);
            }else{
                // this is the LAST DATA Packet, add it to the buffer field:
                Data_ReceivedTillNow_Buffer = getCombinedByteArray(Data_ReceivedTillNow_Buffer, the_data);

                // check if the DATA is for DIRQ\RRQ according to the last_Command:

                if(last_Command[0].equals("DIRQ")){
                    // turn the Data_ReceivedTillNow_Buffer into a String and print the String without the 0's

                    int start = 0;
                    int end = 0;
                    byte[] for_length = new byte[0];
                    for(int i=0; i<Data_ReceivedTillNow_Buffer.length; i++){
                        if((Data_ReceivedTillNow_Buffer[i] == (byte)0) || (i == (Data_ReceivedTillNow_Buffer.length-1))){
                            end = i;
                            if(i == (Data_ReceivedTillNow_Buffer.length-1)){
                                end++;
                            }
                            for_length = Arrays.copyOfRange(Data_ReceivedTillNow_Buffer, start, end);
                            String string_to_print = new String(for_length, 0, for_length.length, StandardCharsets.UTF_8);
                            System.out.println(string_to_print);
                            start = i+1;
                        }
                    }






                }else if(last_Command[0].equals("RRQ")){
                    
                    try{
                        //create file with the Name_of_File_Created name inside the Dir:
                        File f = new File(ClientDir + curr_fileName[0]);
                        boolean created = f.createNewFile();
    
                        // write all the bytes from the buffer field to the file we just created:
                        FileOutputStream fos = new FileOutputStream(ClientDir + curr_fileName[0]);
                        fos.write(Data_ReceivedTillNow_Buffer);
                        fos.close();
    
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                    // now we wrote all the DATA the client gave us that was in the byte buffer, to the file.
                    System.out.println("RRQ " + this.curr_fileName[0] + " complete");

                }
                Data_ReceivedTillNow_Buffer = new byte[0];

            }

            message_back_to_Server = getACKPacket(getShortFrom2ByteArr(Arrays.copyOfRange(message, 4, 6)));  // send ACK.
            should_write_back = true;


















        }else if(opcode == 4){   //*  got an ACK Packet from the Server, probably we should send a DATA Packet conataining (filebytes)\(filenames) and check what was the last_Command, to know
            //                                                                                                                                                                  what to do.

            byte[] block_num_2bytes = new byte[2];
            block_num_2bytes[0] = message[2];
            block_num_2bytes[1] = message[3];
            System.out.println("ACK " + getShortFrom2ByteArr(block_num_2bytes));  // printing the ACK number.

            if(last_Command[0].equals("DISC")){
                should_terminate = true;  // to terminate the while in the run()




            }else if(last_Command[0].equals("WRQ")){

                if(DATA_parts_to_Send.isEmpty()){  // first or last times

                    if(is_first){  // we got a ACK for the WRQ request we made and now we'll start the transferring process  <<--------------------

                        File f = new File(ClientDir + this.curr_fileName[0]);  // already checked in the keyboard thread that file exists

                        int finished_reading = 512;
                        try{
                            try(FileInputStream fis = new FileInputStream(f)){
                                while(finished_reading >=512){
                                    file_bytes = new byte[512]; // the byte buffer
                                    try{
                                        finished_reading = fis.read(file_bytes);   // this int let's us know if the packet is full or not.
                                    }catch(FileNotFoundException e){}catch(IOException e){}

                                    if(finished_reading >= 0){
                                        DATA_parts_to_Send.add(Arrays.copyOfRange(file_bytes, 0, finished_reading));
                                    }else if(finished_reading == -1){
                                        DATA_parts_to_Send.add(new byte[0]);
                                    }
                                }   //  *now the DATA_parts_to_Send has all the data parts of the DATA Packets we'll need to send.
                            }
                        }catch(FileNotFoundException e){
                            e.printStackTrace();
                        }catch(IOException e){
                            e.printStackTrace();
                        }

                        // * build and send the --> FIRST <-- DATA Packet:
                        Block_Number_Count++;  // will be initialized back to 0 in the ACK where we send the last DATA Packet.
                        byte[] dataPart = DATA_parts_to_Send.poll();
                        if(dataPart == null){
                            dataPart = new byte[0];
                        }

                        message_back_to_Server = getDataPacket(dataPart.length, Block_Number_Count, dataPart);
                        should_write_back = true;
                        is_first = false;



                    }else{  // we got an ACK for the last DATA we already sent  <<--------------------

                        System.out.println("WRQ " + this.curr_fileName[0] + " complete");
                        Block_Number_Count = 0;
                        is_first = true;  // reseting for the next WRQ command
                        should_write_back = false;

                    }



                }else{  //* send the next part of data from the sending queue
                        // if there is something to send
                        // * Build another DATA Packet
                        Block_Number_Count++;  // will be initialized back to 0 in the ACK where we send the last DATA Packet.
                        byte[] dataPart = DATA_parts_to_Send.poll();
                        if(dataPart == null){
                            dataPart = new byte[0];
                        }

                        message_back_to_Server = getDataPacket(dataPart.length, Block_Number_Count, dataPart);
                        should_write_back = true;
                    
                }






            }else if(last_Command[0].equals("LOGRQ")){
                // no need to do anything, maybe if needed later on the client side, change a logged_in boolean to true.
            }else if(last_Command[0].equals("DELRQ")){
                // no need to do anything, maybe if something is needed to be done on the client side, then do it. but BCAST is already sent to Clients after this, or Error to this Client.
            }


















            
        }else if(opcode == 5){   //*  got an ERROR Packet from the Server

            // print the ERROR msg in this client:
            byte[] errC2BytesArr = new byte[2];
            errC2BytesArr[0] = message[2];
            errC2BytesArr[1] = message[3];
            short ErrC = getShortFrom2ByteArr(errC2BytesArr);  // short ErrorCode
            String errMsg = new String(message, 4, (message.length-4), StandardCharsets.UTF_8);  //  converting a byte[] to String
            System.out.println("Error " + ErrC + " " + errMsg);  // you said that you only check the error code and the msg is for us.


            // and do stuff that needs to be done:
            if((last_Command[0].equals("RRQ")) && (ErrC == 1)){
                // delete the file we created
                File f = new File(ClientDir + this.curr_fileName[0]);
                f.delete();
            }else if(last_Command[0].equals("DISC")){
                should_terminate = true; //  it's written in the assignment to close even though he's not logged in
            }else if((last_Command[0].equals("DELRQ")) && (ErrC == 1)){
                // only the print above
            }else if((last_Command[0].equals("WRQ")) && (ErrC == 5)){
                // only the print above
            }else if((last_Command[0].equals("LOGRQ")) && (ErrC == 6)){ // user not logged in and some op code received
                // only the print above
            }else if((last_Command[0].equals("LOGRQ")) && (ErrC == 7)){ // user already logged in
                // only the print above
            }  // also for error code: 0, 4, we just use the print above.








        }else if(opcode == 9){   //*  got a BCAST Packet from the Server
            // print the BCAST msg in this client BCAST add/del filename

            // get the filename that was added/deleted and use it for the prints in the next lines
            String packet_fileName = new String(message, 3, (message.length-3), StandardCharsets.UTF_8);  //  converting a byte[] to String

            if(message[2] == (byte)1){  // added
                System.out.println("BCAST add " + packet_fileName);
            }else{   // message[2] == (byte)0  // deleted
                System.out.println("BCAST del " + packet_fileName);
            }

        }







        if(should_write_back){
            try {
                out.write(message_back_to_Server);
                out.flush();
                should_write_back = false; // set it back
            } catch (IOException e) { e.printStackTrace(); }
        }


    }  //*      END OF Client_process





















    public byte[] getDataPacket(int Packet_Size, int Block_Number, byte[] Data_Part){
        byte[] op_2bytes = get2ByteArrFromShort((short)3);
        byte[] packet_size_2bytes = get2ByteArrFromShort((short)Packet_Size);
        byte[] concat1 = getCombinedByteArray(op_2bytes, packet_size_2bytes);  // concat so far, concat with more later

        byte[] block_number_2bytes = get2ByteArrFromShort((short)Block_Number);
        byte[] concat2 = getCombinedByteArray(concat1, block_number_2bytes);  // concat so far, concat with more later

        byte[] concat3 = getCombinedByteArray(concat2, Data_Part);  // concat so far
        return concat3;
    }

    public byte[] getACKPacket(int block_number){
        byte[] op_byte = get2ByteArrFromShort((short)4);
        byte[] block_num_bytes = get2ByteArrFromShort((short)block_number);
        byte[] concat1 = getCombinedByteArray(op_byte, block_num_bytes);  // concat so far
        return concat1;
    }

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
