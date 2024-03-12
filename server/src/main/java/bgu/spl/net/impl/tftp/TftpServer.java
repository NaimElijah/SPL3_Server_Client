package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;


public class TftpServer {

    public static void main(String[] args){
        try{
            Server.threadPerClient(Integer.parseInt(args[0]), () -> new TftpProtocol(), TftpEncoderDecoder::new).serve();
        }catch(NumberFormatException e){
            e.printStackTrace();
        }


    }

}
