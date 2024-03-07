package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;


public class TftpServer {

    public static void main(String[] args){    //TODO: take arguments from maven.
        Server.threadPerClient(7777, () -> new TftpProtocol(), TftpEncoderDecoder::new).serve();


    }

    //TODO: Implement this
}
