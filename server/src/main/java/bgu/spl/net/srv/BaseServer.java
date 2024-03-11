package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.BidiMessagingProtocol;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<BidiMessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;

    private ConnectionsImpl connections;    //* <<-----------------------  keeps all the connected clients
    private Integer id_count;

    public BaseServer(int port, Supplier<BidiMessagingProtocol<T>> protocolFactory, Supplier<MessageEncoderDecoder<T>> encdecFactory){
        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        this.connections = new ConnectionsImpl<byte[]>();  // added by me
        id_count = 0;  // added by me
    }

    @Override
    public void serve(){

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()){      //   ====================>>    This is the main while loop that keeps the server running    <<====================
                Socket clientSock = serverSock.accept();  // just waits for a connection.

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(clientSock, encdecFactory.get(), protocolFactory.get());

                handler.getProtocol().start(id_count, connections);  // added by me
                connections.connect(id_count, handler);   // added by me
                id_count++;  // added by me

                execute(handler);  // runs the connection handler.
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
