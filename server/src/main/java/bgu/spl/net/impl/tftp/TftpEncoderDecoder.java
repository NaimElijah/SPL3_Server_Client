package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {

    private byte[] bytes = new byte[2];
    private int len = 0;

    private int mode = 0;
    private int ByteCounter = 0;
    private byte first_op_byte;
    private byte[] op_C_bytesArr = new byte[2];
    private short small_op_code;

    //TODO: ============>>   Hedi said that the reason he uses hexadecimal is because every byte is represented as 2 hexadecimal numbers. (byte)=(hexnum1hexnum2)   <<============ important !!




    @Override
    public byte[] decodeNextByte(byte nextByte) {     // if I'm using \n it might need to be \r because it's Windows //TODO: notice that we always send a byte[] with the numbers needed !!
        //TODO:   here utf8 is a bit different than linux's, remember to check on the lab computers. for there and for the assignment check, change the \r(windows) back to \n(linux).
        // TODO: implement this
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts, this allow us to do the following comparison.   <-----  copied from echo

        if(ByteCounter == 0){
            first_op_byte = nextByte;
        }else if(ByteCounter == 1){
            op_C_bytesArr[0] = first_op_byte;
            op_C_bytesArr[1] = nextByte;
            small_op_code = getShortFrom2ByteArr(op_C_bytesArr);
            
            if(small_op_code == 3 || small_op_code == 4){ // set to end with null terminator, no ending byte,  //TODO: hedi said that here we should do it with the packet's length
                setMode(0);
            }else if(small_op_code == 7 || small_op_code == 8 || small_op_code == 1 || small_op_code == 2 || small_op_code == 9 || small_op_code == 5) { // set to end with 0 byte
                setMode(1);
            }else if(small_op_code == 6 || small_op_code == 10){ // end, becasue this op code means that this packet has only an op code
                ByteCounter = 0;
                return op_C_bytesArr;
            }
        }

        // if I'm using \n it might need to be \r because it's Windows.
        if(mode == 0 && nextByte == '\0'){  //TODO:  take care of this, every message ends in a different way, this is when it ends with null terminator  <<-----  check null
            //                                                                                              //TODO: hedi said that here we should do it with the packet's length
            byte[] bytesRes = bytes.clone();
            this.bytes = new byte[2];
            len = 0;  // maybe more than this line is needed.
            ByteCounter = 0;
            return bytesRes;   //  in some data, the packet ends when it reaches it's Packet Size, I think here we just get the data and in process() we see to the packet size in the ifs.

        }else if(mode == 1 && nextByte == 00000000){  //TODO:  take care of this, every message ends in a different way, this is when it ends with a 0 byte

            byte[] bytesRes = bytes.clone();
            this.bytes = new byte[2];
            len = 0;  // maybe more than this line is needed.
            ByteCounter = 0;
            return bytesRes;   //  in some data, the packet ends when it reaches it's Packet Size, I think here we just get the data and in process() we see to the packet size in the ifs.

        }

        pushByte(nextByte);
        ByteCounter++;
        return null; //not a line yet
    }


    public void setMode(int mode){
        this.mode = mode;
    }


    public short getShortFrom2ByteArr(byte[] byte2arr){
        return ((short)(((short)(byte2arr[0] & 0xFF)) << 8 | (short)(byte2arr[1] & 0xFF)));
    }  //                                                                                       <<------------  conversion you gave us





    @Override
    public byte[] encode(byte[] message){     // if I'm using \n it might need to be \r because it's Windows
        // here utf8 is a bit different than linux's, remember to check on the lab computers. for there and for the assignment check, change the \r(windows) to \n(linux).
        // return (message + "\n").getBytes(); //uses utf8 by default     <-----------  copied from echo
        return message;  // made by me
    }






    private void pushByte(byte nextByte){
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len + 1);
        }
        bytes[len++] = nextByte;
    }





    // private byte[] popByte(){      //     <-----------  copied from echo
    //     //notice that we explicitly requesting that the string will be decoded from UTF-8  //     <-----------  copied from echo
    //     //this is not actually required as it is the default encoding in java.             //     <-----------  copied from echo
    //     String result = new String(bytes, 0, len, StandardCharsets.UTF_8);          //     <-----------  copied from echo

    //     byte[] resultByteArr = new byte[];  //   <<========================  experimenting........

    //     len = 0;        //     <-----------  copied from echo
    //     return resultByteArr; //     <-----------  experimenting....  maybe use a copy constructor of byte[]
    // }                  //     <-----------  copied from echo



}

