package bgu.spl.net.impl.tftp;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import bgu.spl.net.api.MessageEncoderDecoder;

public class TftpEncoderDecoder implements MessageEncoderDecoder<byte[]> {
    //TODO: Implement here the TFTP encoder and decoder
    private byte[] bytes = new byte[1 << 10]; //start with 1k     <-----------  copied from echo
    private int len = 0;                                   //     <-----------  copied from echo
    private int curr_len_passed = 0; // added by me to determine how much to go or if we finished the message.

    //TODO: ============>>   Hedi said that the reason he uses hexadecimal is because every byte is represented as 2 hexadecimal numbers. (byte)=(hexnum1hexnum2)   <<============ important !!

    @Override
    public byte[] decodeNextByte(byte nextByte) {     // if I'm using \n it might need to be \r because it's Windows //TODO: notice that we always send a byte[] with the numbers needed !!
        //TODO:   here utf8 is a bit different than linux's, remember to check on the lab computers. for there and for the assignment check, change the \r(windows) to \n(linux).
        // TODO: implement this
        //notice that the top 128 ascii characters have the same representation as their utf-8 counterparts, this allow us to do the following comparison.

        //   here we want to convert the encoded byte we got from the client to what we want and add to the byte[] bytes

        if (nextByte == '\n') { //     <-----------  copied from echo          //  do stuff so that if opcode is '0,6' do DIRQ(comes only with opcode).
            return popString(); //     <-----------  copied from echo          //  in some data, the packet ends when it reaches it's Packet Size.
        } //     <-----------  copied from echo

        pushByte(nextByte); //     <-----------  copied from echo
        return null; //not a line yet //     <-----------  copied from echo
    }

    @Override
    public byte[] encode(byte[] message) {     // if I'm using \n it might need to be \r because it's Windows
        //TODO:   here utf8 is a bit different than linux's, remember to check on the lab computers. for there and for the assignment check, change the \r(windows) to \n(linux).
        //TODO: implement this
        // return (message + "\n").getBytes(); //uses utf8 by default     <-----------  copied from echo

        //  this can be a zehut function.  <-------
        return message;  // made by me
    }


    private void pushByte(byte nextByte) {  //     <-----------  copied from echo
        if (len >= bytes.length) {                  //     <-----------  copied from echo
            bytes = Arrays.copyOf(bytes, len * 2);  //     <-----------  copied from echo
        }   //     <-----------  copied from echo

        bytes[len++] = nextByte; //     <-----------  copied from echo
    } //     <-----------  copied from echo

    private String popString() {      //     <-----------  copied from echo
        //notice that we explicitly requesting that the string will be decoded from UTF-8  //     <-----------  copied from echo
        //this is not actually required as it is the default encoding in java.             //     <-----------  copied from echo
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);          //     <-----------  copied from echo
        len = 0;        //     <-----------  copied from echo
        return result; //     <-----------  copied from echo
    }                  //     <-----------  copied from echo



}
