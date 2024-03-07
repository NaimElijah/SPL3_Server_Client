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
    private byte[] file_bytes; // added by me
    private short block_number; // added by me
    private String err_msg; // added by me
    private boolean isBcast = false; // added by me


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

        processed_message = new byte[1 << 10]; //start with 1k      <-----  if a message back is needed right now
        //        it seems like this same protocol will be in the server and client but each of them will probably get into different if's here.


        //*======================================================>>>>  *** Start of the Different scenario handlings ***  <<<<======================================================*/
        //*the processed_message is sent in this method after all these ifs.
        if(opcode != 7 && is_first == true){  //* handle other command before login scenario, Client --->> Server  <<===================================================================  ** BOOKMARK !=7 **
            // return an error to the client that he must use the LOGRQ command first.
            processed_message = getErrPacket(6, "Login Required first");


        }else if(opcode == 1){  //* handle RRQ scenario,  Client --->> Server   <<===================================================================  ** BOOKMARK 1 **

            String packet_fileName = new String(message, 2, (message.length-3), StandardCharsets.UTF_8);  //TODO: do file stuff now where needed in these ifs.
            // see if this file name exists in Files
            File f = new File("../../../../../../../../Files/" + packet_fileName);
            boolean fExists = f.exists();

            if (fExists){
                int finished_reading;
                // return a data packet of the file
                file_bytes = new byte[(int)f.length()]; // maybe the length should be 512 like mentioned in the assignment  or  maybe we should take it all and divide later.
                try {
                    try(FileInputStream fis = new FileInputStream(f)){
                        finished_reading = fis.read(file_bytes);   // do something with this int to know if the packet is full or not.   <<-----------------
                    }
                }catch(FileNotFoundException e){}catch(IOException e){}

                processed_message = new byte[512+2+other thingslikeop,... --> check];  // creating the DATA packet to be sent

                // hedi said that we can send a maximum of 512 bytes at one time. So if it's bigger we need to split them and also send the block
                // number with each part of the data, maybe this handling of block numbers is done in the handler's send.

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









        }else if(opcode == 2){  //* handle WRQ scenario,  Client --->> Server   <<===================================================================  ** BOOKMARK 2 **
            // do more stuff
            // TODO implement this










        }else if(opcode == 3){  //* handle DATA scenario, Both ways   <<===================================================================  ** BOOKMARK 3 **
            // do more stuff
            // TODO implement this









        }else if(opcode == 4){  //* handle ACK scenario   <<===================================================================  ** BOOKMARK 4 **
            if(block_number >= 0 && file_bytes.length > 0){
                // continue sending the next packet
            }else if(another scenario){
                //
            }
            // do more stuff
            // TODO implement this










        }else if(opcode == 5){  //* handle ERROR scenario,  Client <<--- Server   <<===================================================================  ** BOOKMARK 5 **
            // do more stuff
            // TODO implement this









        }else if(opcode == 6){  //* handle DIRQ scenario,  Client --->> Server   <<===================================================================  ** BOOKMARK 6 **
            // do more stuff
            // TODO implement this










        }else if(opcode == 7){  //* handle LOGRQ-login scenario,  Client --->> Server   <<===================================================================  ** BOOKMARK 7 **
            
            if(is_first){
                // theres a method in java for converting a byte[] to string:
                String packet_username = new String(message, 2, (message.length-3), StandardCharsets.UTF_8);
                // check if the username is valid
                boolean valid = true;
                for(ConnectionHandler<byte[]> handler : this.connections.getConnectedClients().values()){
                    if(handler.getProtocol().getUsername() == packet_username){
                        valid = false;
                        break;
                    }
                }
                
                if(valid){
                    this.username = packet_username;
                    connections.getConnectedClients().get(this.connectionId).setLoggedIn(true);
                    is_first = false;
                    // we need to return an ACK packet
                    processed_message = getACKPacket(block number) // I think we need to put 0 in the arg, check in the assignment
                }else{
                    // replay an error that it's not a valid username(the username is already taken).
                    processed_message = getErrPacket(7, "Username already connected");
                }
                is_first = false;
            }else{
                processed_message = getErrPacket(0, "Already logged in");  //  hedi said in the forum that we can do this if a client uses LOGRQ again when he's already logged in.
            }


            // TODO implement this










        }else if(opcode == 8){  //* handle DELRQ scenario,  Client --->> Server   <<===================================================================  ** BOOKMARK 8 **
            String packet_fileName = new String(message, 2, (message.length-3), StandardCharsets.UTF_8);
            // see if this file name exists in Files
            File f = new File("../../../../../../../../Files/" + packet_fileName);
            boolean fExists = f.exists();

            if (fExists){
                // delete that file and send a processed_message if needed
                

            }else{
                processed_message = getErrPacket(1, "File not found");
            }

            // TODO implement this











        }else if(opcode == 9){  //* handle BCAST scenario,  Client <<--- Server   <<===================================================================  ** BOOKMARK 9 **
            // do the op code byte, take code from methods I made
            // op code bytes

            // do the deleted(0) or added(1) byte and concat 1, take code from methods I made
            if(message[2] == 1){  // added
                //
            } // else message[2] == 0 // deleted


            //do the file name bytes and concat 2, take code from methods I made

            // do the zero byte and concat 3, take code from methods I made


            isBcast = true;

            // do more stuff
            // TODO implement this










        }else if(opcode == 10){  //* handle DISC scenario,  Client --->> Server   <<===================================================================  ** BOOKMARK 10 **
            shouldTerminate = true;
            connections.disconnect(connectionId);
            // create an ACK packet with the method we made
            processed_message = getACKPacket(block number) // I think we need to put 0 in the arg, check in the assignment
            // do more stuff if needed
            // TODO implement this


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












    public byte[] getACKPacket(int block_number){
        byte[] op_byte = get2ByteArrFromShort((short)4);
        byte[] block_num_bytes = get2ByteArrFromShort((short)block_number);
        byte[] concat1 = getCombinedByteArray(op_byte, block_num_bytes);  // concat so far
        return concat1;
    }



    public byte[] getErrPacket(int errVal, String err_msg){
        byte[] op_byte = get2ByteArrFromShort((short)5);
        byte[] err_code_bytes = get2ByteArrFromShort((short)errVal);
        // byte[] err_code_bytes = new byte[2];
        // err_code_bytes[0] = (byte)0;
        // err_code_bytes[1] = (byte)errVal;
        byte[] concat1 = getCombinedByteArray(op_byte, err_code_bytes);  // concat so far, concat with more later

        byte[] err_msg_bytes = err_msg.getBytes();
        byte[] concat2 = getCombinedByteArray(concat1, err_msg_bytes);  // concat so far, concat with more later if needed more
        
        byte[] zeroByte = new byte[1];
        zeroByte[0] = (byte)0;
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
