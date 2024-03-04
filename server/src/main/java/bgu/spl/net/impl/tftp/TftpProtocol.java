package bgu.spl.net.impl.tftp;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;


class Holder{
    static ConcurrentHashMap<Integer, Boolean> ids_logged_in = new ConcurrentHashMap<Integer, Boolean>();  // this Holder globally holds the connectionId's and indicates if each client is logged.
}                       //TODO:   I think this hashmap needs to be in the class we are going to implement: "ConnectionsImpl<T> implements Connections<T>" in the srv file.

public class TftpProtocol implements BidiMessagingProtocol<byte[]>  {                                    //TODO: maybe take some inspiration from the echoProtocol

    private boolean shouldTerminate = false;
    private int connectionId;
    private Connections<byte[]> connections;

    //  maybe we need the Connections to be in this protocol.

    @Override
    public void start(int connectionId, Connections<byte[]> connections) {
        shouldTerminate = false;
        this.connectionId = connectionId;
        this.connections = connections;
        Holder.ids_logged_in.put(connectionId, true);
        // maybe we need a Id counter
        // continue
        // TODO implement this

    }

    @Override
    public void process(byte[] message){
        //   here we will take care of all the packets handling and build the processed_message.
        //  ifs to see which packet scenario we're handling

        if(message[0] == 00){  // actually, this is the case for all of them, but just because...

            if(message[1] == 01){  // handle xx scenario
                //



            }else if(message[1] == 02){  // handle xx scenario
                //


            }else if(message[1] == 03){  // handle xx scenario
                //


            }else if(message[1] == 04){  // handle xx scenario
                //


            }else if(message[1] == 05){  // handle xx scenario
                //


            }else if(message[1] == 06){  // handle xx scenario
                //


            }else if(message[1] == 07){  // handle login scenario
                // maybe we need to convert with a function to get the id ()
                Holder.ids_logged_in.put(id, true);
                connections.connect(id, handler);


            }else if(message[1] == 08){  // handle xx scenario
                //


            }else if(message[1] == 09){  // handle broadcast scenario
                if(message[2] == 1){  // added
                    //
                } // else message[2] == 0 // deleted
                //


            }else if(message[1] == 10){  // handle xx scenario
                //


            }
        }

        //  here we actually need to do stuff regarding the files to transfer etc.(process...)

        //  we can read without conversion with the class hedi suggested in the assignment tips.
        
        for(Integer key : Holder.ids_logged_in.keySet()){  // hedi said that we can send a maximum of 512 bytes at one time. So if it's bigger we need to split them and also send the block
        //                                                                                                                            number with each part of the data.
            connections.send(key, processed_message);  // the 'connections' sees which Connection Handler's send() should we use, according to the key(the id).
        }  //  we don't want 2 packets to mix with each other, see that that doesn't happen.

        // TODO implement this

    }

    @Override
    public boolean shouldTerminate() {
        // TODO implement this

        // if(shouldTerminate){
        //     connections.disconnect(connectionId);
        //     Holder.ids_login.remove(connectionId);      if we want it here, I think we can do this somewhere else.
        // }
        return shouldTerminate;
    } 


    
}
