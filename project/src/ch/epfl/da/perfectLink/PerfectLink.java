package ch.epfl.da.perfectLink;

import ch.epfl.da.Callback;
import ch.epfl.da.Process;
import ch.epfl.da.message.Message;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static ch.epfl.da.perfectLink.MessageType.ACK;
import static ch.epfl.da.perfectLink.MessageType.MESSAGE;

/**
 * Implements a perfect link on top of UDP. It uses a mechanism of acks. It 
 * create a thread to receive messages and one thread to retransmit messages.
 *
 */
public class PerfectLink {

    private static final int MAX_SIZE = 1024;
    private static final long MIN_TIMEOUT = 300;
    private final DatagramSocket socket;
    private final Callback<Message> callback;
    private final Thread receive;
    private final Thread retransmit;
    private final Map<Integer, Long> timeouts;
    private final Map<Integer, Boolean> received;
    private final Map<Integer, Tuple> pending;
    private final Set<MessageID> delivered;
    private final Process process;
    private int sequenceNumber;

    public PerfectLink(InetSocketAddress addr, Callback<Message> callBack, Process process) throws SocketException {
        socket = new DatagramSocket(addr);
        this.callback = callBack;
        sequenceNumber = 0;
        pending = new ConcurrentHashMap<>();
        delivered = new HashSet<>();
        receive = receiveThread();
        retransmit = retransmitThread();
        this.process = process;

        timeouts = new ConcurrentHashMap<>();
        received = new ConcurrentHashMap<>();

        for (int i = 0; i < process.getPeers().length; i++) {
            timeouts.put(i + 1, MIN_TIMEOUT);
            received.put(i + 1, false);
        }

    }

    private int getAndIncrement() {
        return sequenceNumber++;
    }

    public void send(Message m, InetSocketAddress target) throws IOException {
        int seq = getAndIncrement();
        PerfectLinkMessage plm = new PerfectLinkMessage(MESSAGE, seq, m);
        DatagramPacket packet = sendPerfectLinkMessage(target, plm);
        int pidTarget = process.pidFromAddr(target);
        pending.put(seq, new Tuple(pidTarget, System.currentTimeMillis(), packet));
    }

    private DatagramPacket sendPerfectLinkMessage(SocketAddress target, PerfectLinkMessage plm) throws IOException {
        byte[] sendBuf = PerfectLinkMessage.serialize(plm);
        DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, target);
        socket.send(packet);
        return packet;
    }

    public void start() {
        receive.start();
        retransmit.start();
    }

    /**
     * Create a thread that receive messages from other processes and deliver them.
     * @return a thread
     */
    private Thread receiveThread() {
        return new Thread() {
            public void run() {
                while (true) {
                    byte[] incomingData = new byte[MAX_SIZE];
                    DatagramPacket packet = new DatagramPacket(incomingData, incomingData.length);
                    try {
                        socket.receive(packet);
                        byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                        PerfectLinkMessage plm = PerfectLinkMessage.deserialize(data);

                        InetSocketAddress source = new InetSocketAddress(packet.getAddress(), packet.getPort());
                        int senderPid = process.pidFromAddr(source);
                        received.put(senderPid, true);

                        if (plm.getType() == MESSAGE) {


                            MessageID id = new MessageID(senderPid, plm.getSeq());
                            if (delivered.add(id)) {
                                callback.onSuccess(plm.getMessage());
                            }

                            sendPerfectLinkMessage(source, plm.getACKfromMessage());
                        } else if (plm.getType() == ACK) {
                            pending.remove(plm.getSeq());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        };
    }

    /**
     * Create a thread that retransmit messages that have not been acked.
     * @return a thread
     */
    private Thread retransmitThread() {
        return new Thread(() -> {
            while (true) {
            	
                for (int i = 0; i < process.getPeers().length; i++) {
                    if (received.get(i + 1)) {
                        timeouts.put(i + 1, MIN_TIMEOUT);
                    }

                    received.put(i + 1, false);
                }

                long now = System.currentTimeMillis();
                boolean[] hasDoubled = new boolean[process.getPeers().length];

                for (Tuple tuple : pending.values()) {
                    long tstamp = tuple.timestamp;
                    long tout = timeouts.get(tuple.PID);
                   
                   

                    if (now > tstamp + tout) {
                        hasDoubled[tuple.PID - 1] = true;

                        tuple.timestamp = now;

                        try {
                            socket.send(tuple.packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                for (int i = 0; i < process.getPeers().length; i++) {
                    if (hasDoubled[i]) {
                        timeouts.computeIfPresent(i + 1, (key, value) -> 2 * value);
                    }
                }

                try {
                    Thread.sleep(MIN_TIMEOUT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class Tuple {
        private int PID;
        private long timestamp;
        private DatagramPacket packet;

        public Tuple(int PID, long timestamp, DatagramPacket packet) {
            this.PID = PID;
            this.timestamp = timestamp;
            this.packet = packet;
        }


    }

}
