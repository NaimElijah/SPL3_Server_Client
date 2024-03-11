package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;


public class TftpServer {

    public static void main(String[] args){    //TODO: take arguments from maven, I think we need to take the port.(args[0/1]).
        Server.threadPerClient(7777, () -> new TftpProtocol(), TftpEncoderDecoder::new).serve();


    }

}
