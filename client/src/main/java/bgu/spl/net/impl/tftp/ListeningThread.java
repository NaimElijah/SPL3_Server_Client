package bgu.spl.net.impl.tftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ListeningThread implements Runnable{

    private boolean should_terminate = false;
    private boolean should_write_back = false;

    private BufferedInputStream in;
    private BufferedOutputStream out;

    private Socket Servers_Socket;
    private TftpEncoderDecoder encdec;
    private Thread keyboardThread;
    private volatile String last_Command;  // if needed later, change this to a queue so mutable and shared with the Keyborad Thread.


    public ListeningThread(Socket s, TftpEncoderDecoder ende){
        this.Servers_Socket = s;
        this.encdec = ende;
    }





    @Override
    public void run(){
        
        KeyboardThread to_write = new KeyboardThread(Servers_Socket, this.last_Command);  // remember this is a runnable object.
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

        keyboardThread.interrupt();  //  if we got a ACK after we made a DISC we'll change the should_terminate to true
    }







    public void Client_process(byte[] message){

        byte[] opc2BytesArr = new byte[2];
        opc2BytesArr[0] = message[0];
        opc2BytesArr[1] = message[1];
        short opcode = getShortFrom2ByteArr(opc2BytesArr);  // short opcode

        byte[] message_back_to_Server = new byte[0];  // just for initialization, set it up according to what we want to send back to the server if want to send something back to the Server.


        if(opcode == 3){   //*  got a DATA Packet from the Server
            //

            // also indicate if we should_write_back=true and set the message_back_to_Server, only if we want to send something back to the Server.
        }else if(opcode == 4){   //*  got a ACK Packet from the Server
            //

            // also indicate if we should_write_back=true and set the message_back_to_Server, only if we want to send something back to the Server.
        }else if(opcode == ){   //*  got a SOME X Packet from the Server
            //

            // also indicate if we should_write_back=true and set the message_back_to_Server, only if we want to send something back to the Server.
        }else if(opcode == ){   //*  got a SOME X Packet from the Server
            //

            // also indicate if we should_write_back=true and set the message_back_to_Server, only if we want to send something back to the Server.
        }
        //? similar to the process of the Server, copy from there... for sending and receiving copy the fields there as well, it's in the SAME CONCEPT !!!!  <<=============================








        if(should_write_back){
            out.write(packet to return to the server);
            out.flush();
            should_write_back = false; // set it back again
        }
    }








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
