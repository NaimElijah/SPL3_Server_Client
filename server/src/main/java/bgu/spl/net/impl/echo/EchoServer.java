package bgu.spl.net.impl.echo;

import bgu.spl.net.srv.Server;

public class EchoServer {

    public static void main(String[] args) {

        // // you can use any server... 
        // Server.threadPerClient(
        //         7777, //port
        //         () -> new EchoProtocol(), //protocol factory                                                     //  because I changed to fit mine.
        //         LineMessageEncoderDecoder::new //message encoder decoder factory        
        // ).serve();

    }
}
