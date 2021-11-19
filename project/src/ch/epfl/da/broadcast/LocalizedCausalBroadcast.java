package ch.epfl.da.broadcast;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ch.epfl.da.Callback;
import ch.epfl.da.Process;
import ch.epfl.da.message.LCBMessage;
import ch.epfl.da.message.Message;

/**
 * Implements Localized Causal Broadcast on top of urb.
 *
 */
public class LocalizedCausalBroadcast {
    private final UniformReliableBroadcast urb;
    private final Process process;
    private final Callback<LCBMessage> callback;
    private final int[] affected;
    private final AtomicIntegerArray received;
    private final AtomicIntegerArray vClock;
    private final List<LCBMessage> pending;

    public LocalizedCausalBroadcast(Process process, Callback<LCBMessage> callback) throws SocketException {
        urb = new UniformReliableBroadcast(process, this::urbDeliver);
        this.process = process;
        this.callback = callback;

        affected = process.getAffectedBy();

        int size = process.getPeers().length;
        received = new AtomicIntegerArray(size);
        vClock = new AtomicIntegerArray(size);

        pending = Collections.synchronizedList(new ArrayList<>());
    }


    /**
     * Broadcast the next message.
     */
    public void broadcast() {
        int pid = process.getPID();
        ByteBuffer buffer = ByteBuffer.allocate(4 * process.getPeers().length);
        for (int i = 0; i < process.getPeers().length; ++i) {
            int value;
            if (i == pid - 1) {
                value = vClock.getAndIncrement(i);
            } else {
                value = vClock.get(i);
            }
            buffer.putInt(value);
        }


        Message toSend = new Message(pid, pid, buffer.array());
        urb.broadcast(toSend);
    }


    /**
     * LCB-deliver a message.
     * @param message the message to deliver
     */
    private void urbDeliver(Message message) {
        int numProcesses = process.getPeers().length;

        LCBMessage lcbMessage = new LCBMessage(message.getOrigin(), message.getData());

        // Get our received vector
        int[] localReceived = new int[numProcesses];
        for (int i = 0; i < numProcesses; ++i) {
            localReceived[i] = received.get(i);
        }

        if (isVectorSmaller(lcbMessage.getData(), localReceived)) { //We can deliver the message
            callback.onSuccess(lcbMessage);

            // Increment received and maybe vClock if the origin affects the process
            localReceived[message.getOrigin() - 1] = incrementVClocks(message.getOrigin());

            // Deliver other messages in pending if possible
            synchronized(pending){
                boolean noMoreToDeliver = false;
                while (!noMoreToDeliver) {
                    noMoreToDeliver = true;

                    Iterator<LCBMessage> it = pending.iterator();
                    while (it.hasNext()) {
                        LCBMessage m = it.next();
                        if (isVectorSmaller(m.getData(), localReceived)) {
                            it.remove();

                            localReceived[m.getOrigin() - 1] = incrementVClocks(m.getOrigin());

                            callback.onSuccess(m);

                            noMoreToDeliver = false;
                        }
                    }
                }
            }
        } else {
            pending.add(lcbMessage);
        }

    }

    /**
     * @param v1 the first vector clock
     * @param v2 the second vector clock
     * @return true if every component of v1 is smaller or equal to v2,
     * false otherwise
     */
    private boolean isVectorSmaller(int[] v1, int[] v2) {
        for (int i = 0; i < v1.length; i++) {
            if (v1[i] > v2[i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * @param origin origin of the message
     * @return the new value of the received vector clock for the
     */
    private int incrementVClocks(int origin) {
        int result = received.incrementAndGet(origin - 1);
        
        if(origin != process.getPID()){
            for (int pid : affected) {
                if (pid == origin) {
                    vClock.incrementAndGet(origin - 1);
                    return result;
                }
            }
        }

        return result;
    }

}
