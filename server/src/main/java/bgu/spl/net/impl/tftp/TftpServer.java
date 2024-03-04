package bgu.spl.net.impl.tftp;

import bgu.spl.net.srv.Server;


public class TftpServer {  // I think this needs to extend BaseServer and ofcourse only implement the abstract execute() method with the threadPerClient one liner I know already.
    

    public static void main(String[] args){    //TODO: take arguments from maven.
        // you can use any server... 
        Server.threadPerClient(
            7777, //port
                () -> new TftpProtocol(), //protocol factory
                TftpEncoderDecoder::new //message encoder decoder factory
                ).serve();


    }

    //TODO: Implement this
}
