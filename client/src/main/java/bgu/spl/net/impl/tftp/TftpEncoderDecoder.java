package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {

    private byte[] bytes = new byte[2];
    private int len = 0;

    private int mode = -1;
    private int ByteCounter = 0;
    private byte first_op_byte;
    private byte[] op_C_bytesArr = new byte[2];
    private short small_op_code;

    private byte first_Size_byte;
    private byte[] Size_bytesArr = new byte[2];
    private short Packet_length;


    @Override
    public byte[] decodeNextByte(byte nextByte) {     // if I'm using \n it might need to be \r because it's Windows
        //TODO:   here utf8 is a bit different than linux's, remember to check on the lab computers. for there and for the assignment check, change the \r(windows) back to \n(linux).

        if(ByteCounter == 0){
            first_op_byte = nextByte;
        }else if(ByteCounter == 1){
            op_C_bytesArr[0] = first_op_byte;
            op_C_bytesArr[1] = nextByte;
            small_op_code = getShortFrom2ByteArr(op_C_bytesArr);
            
            if(small_op_code == 3 || small_op_code == 4){ // set to end with null terminator, no ending byte,  //TODO: hedi said that here we should do it with the packet's length
                setMode(0);
                if(small_op_code == 4){
                    Packet_length = 3;
                }
            }else if(small_op_code == 7 || small_op_code == 8 || small_op_code == 1 || small_op_code == 2 || small_op_code == 9 || small_op_code == 5) { // set to end with 0 byte
                setMode(1);
            }else if(small_op_code == 6 || small_op_code == 10){ // end, becasue this op code means that this packet has only an op code
                ByteCounter = 0;
                Packet_length = 0;
                len = 0;
                this.bytes = new byte[2];
                mode = -1;
                small_op_code = 0;
                return op_C_bytesArr;
            }
        }




        if(ByteCounter == 2 && small_op_code == 3){  //  to get the DATA size
            first_Size_byte = nextByte;
        }else if(ByteCounter == 3 && small_op_code == 3){
            Size_bytesArr[0] = first_Size_byte;
            Size_bytesArr[1] = nextByte;
            Packet_length = getShortFrom2ByteArr(Size_bytesArr);  //  to get the DATA size
            Packet_length += 5;  // for the 6 opening bytes of DATA
        }






        if(mode == 0 && (ByteCounter >= Packet_length)){  //  this is when we end it according to the length.
            pushByte(nextByte);
            ByteCounter++;
            byte[] bytesRes = bytes.clone();
            this.bytes = new byte[2];
            len = 0;
            ByteCounter = 0;
            Packet_length = 0;
            mode = -1;
            small_op_code = 0;
            return bytesRes;

        }else if(mode == 1 && nextByte == (byte)0){  // this is when it ends with a 0 byte
            byte[] bytesRes = bytes.clone();
            this.bytes = new byte[2];
            len = 0;
            ByteCounter = 0;
            Packet_length = 0;
            mode = -1;
            small_op_code = 0;
            return bytesRes;
        }




        pushByte(nextByte);
        ByteCounter++;
        return null; // not a line yet
    }






    public void setMode(int mode){
        this.mode = mode;
    }


    public short getShortFrom2ByteArr(byte[] byte2arr){
        return ((short)(((short)(byte2arr[0] & 0xFF)) << 8 | (short)(byte2arr[1] & 0xFF)));
    }  //                                                                                       <<------------  conversion you gave us





    @Override
    public byte[] encode(byte[] message){
        return message;  // made by me
    }






    private void pushByte(byte nextByte){
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len + 1);
        }
        bytes[len++] = nextByte;
    }




}

