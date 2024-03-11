package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>{

    private boolean shouldTerminate = false;
    private String ServerDir = ".\\Files\\";   //TODO:  maybe change to singular \'s for the assignment check that will probably happen on linux.   <<-------------------------------
    private int connectionId;
    private String username;
    private Connections<byte[]> connections;  //  this is our way to reach the Connection Handlers's Handler --> ConnectionsImpl.(for send, etc.)     <<--------------------------
    private byte[] processed_message;
    private boolean is_first = true;
    private short opcode;
    private boolean isBcast = false;

    private byte[] file_bytes; // byte BUFFER WHEN SERVER READS FROM HIS Files
    private ConcurrentLinkedQueue<byte[]> DATA_parts_to_Send = new ConcurrentLinkedQueue<byte[]>();  //? <<---------------- for WRITING to the client.
    private short Block_Number_Count = 0;
    // * send 512(518 with others) data max every time and the receiver keeps receiving until he gets something that isn't full, that isn't 512(518) bytes.


    private String Name_of_File_Created;
    private byte[] Data_ReceivedTillNow_Buffer = new byte[0];  //? <<---------------- for READING from the client.
    // * keep all the bytes received in the buffer until we get something that isn't full, that isn't 512(518) bytes, add it as well and then write it all into the Files of the Server.


    @Override
    public void start(int connectionId, Connections<byte[]> connections){
        shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
    }




    @Override
    public void process(byte[] message){
        byte[] opc2BytesArr = new byte[2];
        opc2BytesArr[0] = message[0];
        opc2BytesArr[1] = message[1];
        opcode = getShortFrom2ByteArr(opc2BytesArr);  // short opcode

        processed_message = new byte[1 << 10]; // just for initialization (1k)




        //*======================================================>>>>  *** Start of the Different Scenario Handlings ***  <<<<======================================================*/
        //* The processed_message is sent in this method after all these ifs.

        if((opcode != 7) && (is_first == true) && (opcode > 0) && (opcode < 11)){  //* handle other command before login scenario, Server got this  <<===================================================================  ** BOOKMARK !=7 **
            // return an error to the client that he must use the LOGRQ command first.
            processed_message = getErrPacket(6, "Login Required first");







        }else if(opcode == 1){  //* handle RRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 1 **

            String packet_fileName = new String(message, 2, (message.length-2), StandardCharsets.UTF_8);
            // see if this file name exists in Files
            File f = new File(ServerDir + packet_fileName);
            boolean fExists = f.exists();

            if(fExists){
                int finished_reading = 512;
                try{
                    try(FileInputStream fis = new FileInputStream(f)){
                        synchronized(connections.getFilesLock()){  // Lock the Files while we're reading from them
                            while(finished_reading >=512){
                                file_bytes = new byte[512]; // the byte buffer
                                try{
                                    finished_reading = fis.read(file_bytes);   // this int let's us know if the packet is full or not.
                                }catch(FileNotFoundException e){}catch(IOException e){}

                                if(finished_reading > 0){
                                    DATA_parts_to_Send.add(file_bytes);
                                }
                            }   //  *now the DATA_parts_to_Send has all the data parts of the DATA Packets we'll need to send.
                        }
                    }
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }


                // *build the --> FIRST <-- DATA Packet:
                Block_Number_Count++;  // will be initialized back to 0 in the ACK where we send the last DATA Packet.
                byte[] dataPart = DATA_parts_to_Send.poll();
                if(dataPart == null){
                    dataPart = new byte[0];
                }

                processed_message = getDataPacket(dataPart.length, Block_Number_Count, dataPart);

                // hedi said that we can send a maximum of 512 bytes at one time. So if it's bigger we need to split them and also send the block
                // number with each part of the data.
            }else{
                processed_message = getErrPacket(1, "File not found");
            }











        }else if(opcode == 2){  //* handle WRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 2 **

            // send ACK or ERR and then get the DATA from the Client in the DATA if.
            String packet_fileName = new String(message, 2, (message.length-2), StandardCharsets.UTF_8);
            // see if this file name exists in Files
            File f = new File(ServerDir + packet_fileName);
            boolean fExists = f.exists();

            if(fExists){
                processed_message = getErrPacket(5, "File already exists");
            }else{
                Name_of_File_Created = packet_fileName;
                processed_message = getACKPacket(0);
            }





            




        }else if(opcode == 3){  //* handle DATA scenario, when the SERVER gets a DATA Packet after WRQ <<===================================================================  ** BOOKMARK 3 **
            //* SO the ONLY DATA WE GET IS FOR WRQ !!!!!!! */
            //* This is AFTER WRQ and ACK sent to the Client, now the Client will send DATA Packets, receive them one by one until we get a below 512 bytes sized DATA Packet:

            // get the DATA byte[]:
            byte[] the_data = Arrays.copyOfRange(message, 6, message.length);

            if(the_data.length >= 512){
                // we got a full DATA Packet, add the DATA byte[] to the buffer field
                Data_ReceivedTillNow_Buffer = getCombinedByteArray(Data_ReceivedTillNow_Buffer, the_data);
                processed_message = getACKPacket(getShortFrom2ByteArr(Arrays.copyOfRange(message, 4, 6)));
            }else{
                // this is the LAST DATA Packet, add it to the buffer field:
                Data_ReceivedTillNow_Buffer = getCombinedByteArray(Data_ReceivedTillNow_Buffer, the_data);

                synchronized(connections.getFilesLock()){  // lock the file while writing to it at the end
                    try{
                        //create file with the Name_of_File_Created name inside the Dir:
                        File f = new File(ServerDir + Name_of_File_Created);
                        boolean created = f.createNewFile();

                        // write all the bytes from the buffer field to the file we just created:
                        FileOutputStream fos = new FileOutputStream(ServerDir + Name_of_File_Created);
                        fos.write(Data_ReceivedTillNow_Buffer);
                        fos.close();

                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }    // now we wrote all the DATA the client gave us that was in the byte buffer, to the file.


                connections.send(this.connectionId, getACKPacket(getShortFrom2ByteArr(Arrays.copyOfRange(message, 4, 6))));
                processed_message = getBCASTPacket(1, Name_of_File_Created);  // notify all logged in users about the change
                isBcast = true;

            }












        }else if(opcode == 4){  //* handle ACK scenario   <<===================================================================  ** BOOKMARK 4 **
            byte[] blockNumber2Bytes = new byte[2];
            blockNumber2Bytes[0] = message[2];
            blockNumber2Bytes[1] = message[3];
            short packet_block_num = getShortFrom2ByteArr(blockNumber2Bytes);

            if(packet_block_num == 0){

                //! DON'T NEED THIS SECTION because the Server won't get this block number=0, only on the Client Side.{
                // for the cases where we just get a confirmation about other stuff like LOGRQ, WRQ, DELRQ, DISC...:
                // after LOGRQ, we're in Client, nothing, just confirmation
                // after DELRQ, we're in Client, nothing, just confirmation
                // after DISC, we're in Client, maybe change a boolean field I'll make here in the protocol to indicate that disconnected and then the Client will know to shutdown.
                //TODO: DON'T NEED THIS SECTION because the Server won't get this block number=0, only on the Client Side.
                // after WRQ, we're in the Client:
                // we need to send DATA to the Server, check how to send to the Server, maybe kind of the same way that we send to a Client, maybe.
                //! DON'T NEED THIS SECTION because the Server won't get this block number=0, only on the Client Side.}



            }else{ //* after RRQ, DIRQ, we're in Server, we got the number of the DATA block the other received, now send the next DATA if there is more DATA to send.  <<=========== SERVER SIDE.
                if(!(DATA_parts_to_Send.isEmpty())){ // if there is something to send, if there isn't more to send, the other side will notice that before us when he get's size below 512.
                //                                                                                                                                                 in the DATA handling if.
                    // * Build another DATA Packet
                    Block_Number_Count++;  // will be initialized back to 0 in the ACK where we send the last DATA Packet.
                    byte[] dataPart = DATA_parts_to_Send.poll();
                    if(dataPart == null){
                        dataPart = new byte[0];
                    }

                    processed_message = getDataPacket(dataPart.length, Block_Number_Count, dataPart);
                
                }else{  // DATA_parts_to_Send is empty, this is the last ACK for the last DATA Packet. other side already knows from the DATA handling if that he got the last one.
                    Block_Number_Count = 0;
                }
            }













        }else if(opcode == 5){  //! ERROR scenario,  Client gets this, Server doesn't get Error   <<===================================================================  ** BOOKMARK 5 **
            
            //TODO:  MAYBE DELETE THIS,  the Server doesn't get ERROR, maybe we should also Delete this if so it will go to the Section where unknown op code{
            byte[] errC2BytesArr = new byte[2];
            errC2BytesArr[0] = message[2];
            errC2BytesArr[1] = message[3];
            short ErrC = getShortFrom2ByteArr(errC2BytesArr);  // short ErrorCode
            //TODO:  MAYBE DELETE THIS,  the Server doesn't get ERROR, maybe we should also Delete this if so it will go to the Section where unknown op code
            String err_msg = new String(message, 4, (message.length-4), StandardCharsets.UTF_8);  //  converting a byte[] to String
            System.out.println("Error " + ErrC + " " + err_msg);

            //?  Server doesn't get an ERROR but did this just because...
            //TODO:  MAYBE DELETE THIS,  the Server doesn't get ERROR, maybe we should also Delete this if so it will go to the Section where unknown op code}










        }else if(opcode == 6){  //* handle DIRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 6 **

            List<byte[]> FilesInDirInBytes = new ArrayList<byte[]>();
            File[] files = new File(ServerDir).listFiles();
            for(File file : files){
                if(file.isFile()){
                    FilesInDirInBytes.add(file.getName().getBytes());
                }
            }    // now got all the file names's byte[] forms in a list.


            // prepare all the data in one byte[] including the 0 bytes in between the filenames.
            byte[] all_bytes_with0Between_toSend = new byte[0];
            for(byte[] one_cell_bytes_arr : FilesInDirInBytes){
                all_bytes_with0Between_toSend = getCombinedByteArray(all_bytes_with0Between_toSend, one_cell_bytes_arr);
                byte[] zero_byte = new byte[1];
                zero_byte[0] = (byte)0;
                all_bytes_with0Between_toSend = getCombinedByteArray(all_bytes_with0Between_toSend, zero_byte);
            }
            all_bytes_with0Between_toSend = Arrays.copyOfRange(all_bytes_with0Between_toSend, 0, (all_bytes_with0Between_toSend.length-1));  // getting rid of the ending 0 byte
            //  now got all of the bytes to send together in one byte[].


            // put all of the bytes in the queue DATA_parts_to_Send
            while(all_bytes_with0Between_toSend.length > 0){
                if(all_bytes_with0Between_toSend.length > 512){
                    DATA_parts_to_Send.add(Arrays.copyOfRange(all_bytes_with0Between_toSend, 0, 512)); // adding 512 bytes to a cell in the Sending queue.
                    all_bytes_with0Between_toSend = Arrays.copyOfRange(all_bytes_with0Between_toSend, 512, all_bytes_with0Between_toSend.length); // removing the first 512 from it.
                }else{
                    DATA_parts_to_Send.add(all_bytes_with0Between_toSend);
                    break;
                }
            }  // now all the data is in the sending queue DATA_parts_to_Send, divided so each cell has 512 bytes and the last one has the remainder left when we got to the end.


            // send the FIRST DATA WITH FILENAMES:   // the rest will be sent in the ACK
            Block_Number_Count++;  // will be initialized back to 0 in the ACK where we send the last DATA Packet.
            byte[] dataPart = DATA_parts_to_Send.poll();
            if(dataPart == null){
                dataPart = new byte[0];
            }

            processed_message = getDataPacket(dataPart.length, Block_Number_Count, dataPart);









        }else if(opcode == 7){  //* handle LOGRQ-login scenario,  Server got this   <<===================================================================  ** BOOKMARK 7 **
            
            if(is_first){
                String packet_username = new String(message, 2, (message.length-2), StandardCharsets.UTF_8);  //  converting a byte[] to String
                boolean valid = true;  // check if the username is valid
                for(ConnectionHandler<byte[]> handler : this.connections.getConnectedClients().values()){
                    if(handler.getProtocol().getUsername() == packet_username){
                        valid = false;
                        break;
                    }
                }
                
                if(valid){
                    this.username = packet_username;
                    connections.getConnectedClients().get(this.connectionId).setLoggedIn(true);
                    processed_message = getACKPacket(0); // ACK to the Client that we received
                    is_first = false;
                }else{
                    processed_message = getErrPacket(7, "Username already connected");
                }
            }else{
                processed_message = getErrPacket(0, "Already logged in");  //  hedi said in the forum that we can do this if a client uses LOGRQ again when he's already logged in.
            }   //                                                                     check again which error type to return.





        }else if(opcode == 8){  //* handle DELRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 8 **
            String packet_fileName = new String(message, 2, (message.length-2), StandardCharsets.UTF_8);
            // see if this file name exists in Files
            File f = new File(ServerDir + packet_fileName);
            boolean fExists = f.exists();

            if (fExists){
                // delete that file and send a BCAST processed_message
                if(f.delete()){
                    processed_message = getBCASTPacket(0, packet_fileName);  // notify all logged in users about the change
                    isBcast = true;
                }else{
                    processed_message = getErrPacket(0, "Inner Deletion Fail");
                }
            }else{
                processed_message = getErrPacket(1, "File not found");
            }



        }else if(opcode == 9){  //! BCAST scenario,  Client gets this, Server DOESN'T get this   <<===================================================================  ** BOOKMARK 9 **

            //TODO:    DELETE THIS,  the Server doesn't get BCAST, maybe we should also Delete this if so it will go to the Section where unknown op code
            // get the filename that was added/deleted and use it for the prints in the next lines

            // String packet_fileName = new String(message, 3, (message.length-3), StandardCharsets.UTF_8);  //  converting a byte[] to String

            // if(message[2] == (byte)1){  // added
            //     System.out.println("BCAST add " + packet_fileName);  // each client printing to himself                 <<-------------  should be in the Client Side.
            // }else{   // message[2] == (byte)0  // deleted
            //     System.out.println("BCAST del " + packet_fileName);  // each client printing to himself
            // }



        }else if(opcode == 10){  //* handle DISC scenario,  Server got this   <<===================================================================  ** BOOKMARK 10 **
            processed_message = getACKPacket(0);  // when the client gets this ACK he will close the socket and exit the client program, I think he will also just interrupt
            connections.disconnect(connectionId);                                                  // the threads maybe or just make a boolean in the run()'s while to exit while.
            shouldTerminate = true;


        }else{  //  unknown op code inserted
            processed_message = getErrPacket(4, "Unknown Op Code");
        }






        //*    here we're sending the processed message after all the ifs in this method:

        if(processed_message.length > 0){  // check if a processed_message needs to be sent\has been made for it to be sent
            if(isBcast){
                for(Integer key : connections.getConnectedClients().keySet()){
                    if(connections.getConnectedClients().get(key).getLoggedIn()){  // send only to those who are logged in
                        connections.send(key, processed_message);
                    }
                }
                isBcast = false;
            }else{
                connections.send(this.connectionId, processed_message);  // returns boolean, but the boolean is not needed here     <<---------------------  sending the processed message
            }
        }


    }  //*  END OF PROCESS METHOD.















    public byte[] getDataPacket(int Packet_Size, int Block_Number, byte[] Data_Part){
        byte[] op_2bytes = get2ByteArrFromShort((short)3);
        byte[] packet_size_2bytes = get2ByteArrFromShort((short)Packet_Size);
        byte[] concat1 = getCombinedByteArray(op_2bytes, packet_size_2bytes);  // concat so far, concat with more later

        byte[] block_number_2bytes = get2ByteArrFromShort((short)Block_Number);
        byte[] concat2 = getCombinedByteArray(concat1, block_number_2bytes);  // concat so far, concat with more later

        byte[] concat3 = getCombinedByteArray(concat2, Data_Part);  // concat so far
        return concat3;
    }




    public byte[] getBCASTPacket(int addORdel, String Filename){
        byte[] op_byte = get2ByteArrFromShort((short)9);
        byte[] addORdelByte = new byte[1];
        addORdelByte[0] = (byte)addORdel;  // *check if maybe another way of conversion is needed here, if this way doesn't work out
        byte[] concat1 = getCombinedByteArray(op_byte, addORdelByte);  // concat so far, concat with more later

        byte[] filename_bytes = Filename.getBytes();
        byte[] concat2 = getCombinedByteArray(concat1, filename_bytes);  // concat so far
        
        byte[] zeroByte = new byte[1];
        zeroByte[0] = (byte)0;
        byte[] concat3 = getCombinedByteArray(concat2, zeroByte);
        return concat3;
    }



    public byte[] getACKPacket(int block_number){
        byte[] op_byte = get2ByteArrFromShort((short)4);
        byte[] block_num_bytes = get2ByteArrFromShort((short)block_number);
        byte[] concat1 = getCombinedByteArray(op_byte, block_num_bytes);  // concat so far
        return concat1;
    }


    public byte[] getErrPacket(int errVal, String err_msg){
        byte[] op_byte = get2ByteArrFromShort((short)5);
        byte[] err_code_bytes = get2ByteArrFromShort((short)errVal);
        byte[] concat1 = getCombinedByteArray(op_byte, err_code_bytes);  // concat so far, concat with more later

        byte[] err_msg_bytes = err_msg.getBytes();
        byte[] concat2 = getCombinedByteArray(concat1, err_msg_bytes);  // concat so far, concat with more later if needed more
        
        byte[] zeroByte = new byte[1];
        zeroByte[0] = (byte)0;  // *check if maybe another way of conversion is needed here, if this way doesn't work out
        byte[] concat3 = getCombinedByteArray(concat2, zeroByte);
        return concat3;
    }


    public byte[] getCombinedByteArray(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }


    public String getUsername(){
        return this.username;
    }


    public short getShortFrom2ByteArr(byte[] byte2arr){
        // return (short)(((short) byte2arr[0]) << 8 | (short) (byte2arr[1]) & 0x00ff);    // hedi's 
        return ((short)(((short)(byte2arr[0] & 0xFF)) << 8 | (short)(byte2arr[1] & 0xFF)));
    }  //                                                                                       <<------------  conversions you gave us

    public byte[] get2ByteArrFromShort(short shortNum){
        return (new byte[]{(byte)(shortNum >> 8), (byte)(shortNum & 0xff)});  // might be without the "="
    }



    @Override
    public boolean shouldTerminate(){
        return shouldTerminate;
    } 
    
}
