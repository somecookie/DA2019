package ch.epfl.da;

import static ch.epfl.da.io.Parser.parseID;
import static ch.epfl.da.io.Parser.parseNumMessages;
import static ch.epfl.da.io.Parser.parseProcesses;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import ch.epfl.da.broadcast.FIFOBroadcast;
import ch.epfl.da.broadcast.LocalizedCausalBroadcast;
import ch.epfl.da.message.FIFOMessage;
import ch.epfl.da.io.Logger;
import ch.epfl.da.message.Message;

/**
 * Main class of the program. Initialize and wait for USR2 signal
 * before broadcasting
 *
 */
public class Main {
    private static final int INT_SIZE = 4;
    private static final int NUM_ARGS = 3;
    private static final int ARG_PID = 0;
    private static final int ARG_MEMBERSHIP = 1;
    private static final int ARG_NUM_MSG = 2;


    public static void main(String[] args) throws IOException {
        // check args
        Process[] processes;
        int pID;
        int numMessages;
        if (args.length < NUM_ARGS) {
            throw new IllegalArgumentException("Not enough arguments to start the process");
        }

        // parse process ID
        pID = parseID(args[ARG_PID]);

        // initialize logger
        Logger logger = new Logger(pID);

        // set signal handlers
        ProcessSigHandler handler = new ProcessSigHandler(logger);

        // parse other arguments, including membership
        numMessages = parseNumMessages(args[ARG_NUM_MSG]);
        processes = parseProcesses(pID, args[ARG_MEMBERSHIP]);
        
        //The initial number of msg to send
        

        // initialize the current process and its FIFO broadcast
        Process process = processes[pID - 1]; 
        LocalizedCausalBroadcast lcb = new LocalizedCausalBroadcast(process,
        		msg -> {
        			int origin = msg.getOrigin();
        			int value = msg.getValue();
        			try {
						logger.deliver(origin, value);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        			
        		});
        

        // wait to receive the USR2 starting signal
        while (handler.waitForStart().get()) ;

        
        // broadcast messages via FIFOBroadcast
        for (int i = 1; i <= numMessages; ++i) {
        	
        	lcbroadcast(pID, logger, lcb, i);
        }

    }


	private static void lcbroadcast(int pID, Logger logger, LocalizedCausalBroadcast lcb, int i) throws IOException {
		logger.broadcast(i);
		lcb.broadcast();
	}

}
