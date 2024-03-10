package bgu.spl.net.impl.tftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;

public class TftpProtocol implements BidiMessagingProtocol<byte[]>{

    private boolean shouldTerminate = false;
    private int connectionId;
    private String username;  // <----  added by me, may also be needed for the username
    private Connections<byte[]> connections;  //  this is our way to reach the Connection Handlers's Handler --> ConnectionsImpl.(for send, etc.)     <<--------------------------
    private byte[] processed_message; // added by me
    private boolean is_first = true; // added by me
    private short opcode; // added by me
    private short block_number; // added by me
    private boolean isBcast = false; // added by me

    private byte[] file_bytes; // added by me  //TODO: Added by me, see if this way works out    <<--------------------------------
    private int file_bytes_bookmark = 0; // added by me  //TODO: Added by me, see if this way works out    <<--------------------------------

    private byte[] Data_LeftToSend;  //TODO: Added by me, see if this way works out    <<--------------------------------
    private byte[] Data_ReceivedTillNow;  //TODO: Added by me, see if this way works out    <<--------------------------------
    // maybe send 512(518 with others) data max and every time and the receiver keeps receiving until he gets something that isn't full, that isn't 512(518) bytes.
    


    @Override
    public void start(int connectionId, Connections<byte[]> connections){
        shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        // more if needed
    }




    @Override
    public void process(byte[] message){
        // it seems like, when we need to access the Files, we need to access .\Files
        byte[] opc2BytesArr = new byte[2];
        opc2BytesArr[0] = message[0];
        opc2BytesArr[1] = message[1];
        opcode = getShortFrom2ByteArr(opc2BytesArr);  // short opcode

        processed_message = new byte[1 << 10]; // just for initialization (1k)
        //        it seems like this same protocol will be in the server and client but each of them will probably get into different if's here.





        //*======================================================>>>>  *** Start of the Different Scenario Handlings ***  <<<<======================================================*/
        //* The processed_message is sent in this method after all these ifs.

        if((opcode != 7) && (is_first == true) && (opcode > 0) && (opcode < 11)){  //* handle other command before login scenario, Server got this  <<===================================================================  ** BOOKMARK !=7 **
            // return an error to the client that he must use the LOGRQ command first.
            processed_message = getErrPacket(6, "Login Required first");








        }else if(opcode == 1){  //* handle RRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 1 **

            String packet_fileName = new String(message, 2, (message.length-2), StandardCharsets.UTF_8);
            // see if this file name exists in Files
            File f = new File("./Files/" + packet_fileName);
            boolean fExists = f.exists();

            if (fExists){
                int finished_reading;

                synchronized(connections.getFilesLock()){  // Lock the Files while we're reading from them

                    // file_bytes = new byte[518];   //TODO: check this way out
                    // maybe add to a ConcurrentLinkedQueue<byte[]> field   //TODO: check this way out
                    // while(fis.read(file_bytes) != -1){}   //TODO: check this way out
                    // file_bytes = new byte[512];   //TODO: check this way out
                    // maybe add to a ConcurrentLinkedQueue<byte[]> field   //TODO: check this way out
                    // and somewhere do connections.send to send and then when we get an ACK from the Client we can see what's remaining //TODO: check this way out, take some of the below code:
                    

                    file_bytes = new byte[(int)f.length()]; // maybe we should take it all and divide later.
                    try {
                        try(FileInputStream fis = new FileInputStream(f)){
                            finished_reading = fis.read(file_bytes);   // do something with this int to know if the packet is full or not.   <<-----------------
                        }            // now file_bytes contains all the file bytes
                    }catch(FileNotFoundException e){}catch(IOException e){}

                }

                //TODO: return data packets of the file
                // hedi said that we can send a maximum of 512 bytes at one time. So if it's bigger we need to split them and also send the block
                // number with each part of the data, maybe this handling of block numbers is done in the handler's send.

                if(finished_reading >= 518){
                    //
                }else{
                    //
                }

                processed_message = new byte[512+6];  // creating the DATA packet to be sent

                short returnOP_C = 3;
                byte[] returnOP_C_in_byteArr = get2ByteArrFromShort(returnOP_C);
                processed_message[0] = returnOP_C_in_byteArr[0];
                processed_message[1] = returnOP_C_in_byteArr[1];

                short data_sec_size = (short)finished_reading;  // to convert to 2 bytes array for packet size
                if(finished_reading == ){
                    //
                }else{
                    //
                }
                

            }else{
                processed_message = getErrPacket(1, "File not found");
            }


            
            // do more stuff if needed
            // TODO implement this










        }else if(opcode == 2){  //* handle WRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 2 **
            // I think that here we need to first create a file with the name, send ACK and then get the DATA from the Client.



            // we need to put all the bytes of data from the data packets we got into a buffer and only at the end sync the Files and write to Files within that sync
            synchronized(connections.getFilesLock()){  // maybe lock the file while writing to it at the end
                //
            }
            



            processed_message = getBCASTPacket(1, put filenameeeeeeeeeeeeeeeeeeeeeeeeeeeee);  // notify all logged in users about the change
            isBcast = true;

            
            // do more stuff
            // TODO implement this












        }else if(opcode == 3){  //* handle DATA scenario, Both get this   <<===================================================================  ** BOOKMARK 3 **

            // maybe put all the DATA Packages in a byte buffer

            // do more stuff
            // TODO implement this











        }else if(opcode == 4){  //* handle ACK scenario, Both get this   <<===================================================================  ** BOOKMARK 4 **
            if(block_number >= 0 && file_bytes.length > 0){
                // continue sending the next packet
            }else if(another scenario){
                //
            }
            // do more stuff
            // TODO implement this













        }else if(opcode == 5){  //* handle ERROR scenario,  Client gets this   <<===================================================================  ** BOOKMARK 5 **
            byte[] errC2BytesArr = new byte[2];
            errC2BytesArr[0] = message[2];
            errC2BytesArr[1] = message[3];
            short ErrC = getShortFrom2ByteArr(errC2BytesArr);  // short ErrorCode

            String err_msg = new String(message, 4, (message.length-4), StandardCharsets.UTF_8);  //  converting a byte[] to String
            System.out.println("Error " + ErrC + " " + err_msg);









        }else if(opcode == 6){  //* handle DIRQ scenario,  Server got this   <<===================================================================  ** BOOKMARK 6 **
            
            List<byte[]> FilesInDirInBytes = new ArrayList<byte[]>();
            int len = 0;
            File[] files = new File("./Files").listFiles();
            for(File file : files){
                if(file.isFile()){
                    FilesInDirInBytes.add(file.getName().getBytes());
                    len += file.getName().getBytes().length;
                }
            }


            // we also need to send a DATA Package
            if(len <= 512){  // or 518
                //
            }else{
                //
            }




            // do more stuff
            // TODO implement this












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
            File f = new File("./Files/" + packet_fileName);
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



        }else if(opcode == 9){  //* handle BCAST scenario,  Client got this   <<===================================================================  ** BOOKMARK 9 **

            // get the filename that was added/deleted and use it for the prints in the next lines
            String packet_fileName = new String(message, 3, (message.length-3), StandardCharsets.UTF_8);  //  converting a byte[] to String

            if(message[2] == (byte)1){  // added
                System.out.println("BCAST add " + packet_fileName);  // each client printing to himself
            }else{   // message[2] == (byte)0  // deleted
                System.out.println("BCAST del " + packet_fileName);  // each client printing to himself
            }



        }else if(opcode == 10){  //* handle DISC scenario,  Server got this   <<===================================================================  ** BOOKMARK 10 **
            shouldTerminate = true;
            connections.disconnect(connectionId);
            processed_message = getACKPacket(0);
            

        }else{  //  unknown op code inserted
            processed_message = getErrPacket(4, "Unknown Op Code");
        }










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


        //          hedi said that we can send a maximum of 512 bytes at one time. So if it's bigger we need to split them and also send the block
        //          number with each part of the data, maybe this handling of block numbers is done in the handler's send
        //          we don't want 2 packets to mix with each other, see that that doesn't happen.

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
