package ch.epfl.da.perfectLink;

import ch.epfl.da.message.Message;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * This class represents the packets sent across the perfect link.
 */
public class PerfectLinkMessage {
    private MessageType type;
    private int seq;
    private Message message;
    private int hash;

    public PerfectLinkMessage() {
    }

    public PerfectLinkMessage(MessageType type, int seq, Message data) {
        this.type = type;
        this.seq = seq;
        this.message = data;

        hash = seq;
        if (type == MessageType.MESSAGE) {
            hash += data.hashCode();
        }
    }

    /**
     * serialize transforms this into an array of bytes
     * The structure of the array is the following:
     * -----------PerfectLinkMessage--------------
     * 0: type (1 for message and 0 for ACK)
     * 1-4: seq
     * --------Encapsulated message---------------
     * 5-8: origin
     * 9-12: senderPID
     * 13-...: data
     * -------------------------------------------
     *
     * @param plm the PerfectLinkMessage we want to serialize
     * @return the serialized form of plm that can then be sent through the network
     */
    public static byte[] serialize(PerfectLinkMessage plm) {
        byte[] bytes = new byte[1];
        bytes[0] = (plm.type == MessageType.ACK) ? (byte) 0 : 1;

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(plm.seq);
        bytes = concatenate(bytes, bb.array());

        if (plm.type == MessageType.MESSAGE) {
            bb.clear();
            bb.putInt(plm.message.getOrigin());
            bytes = concatenate(bytes, bb.array());

            bb.clear();
            bb.putInt(plm.message.getSenderPID());
            bytes = concatenate(bytes, bb.array());

            bytes = concatenate(bytes, plm.message.getData());
        }

        return bytes;
    }

    /**
     * deserialize transforms the byte array passed as parameter into PerfectLinkMessage
     * The structure of the array is the following:
     * -----------PerfectLinkMessage--------------
     * 0: type (1 for message and 0 for ACK)
     * 1-4: seq
     * --------Encapsulated message---------------
     * 5-8: origin
     * 9-12: senderPID
     * 13-...: data
     * -------------------------------------------
     *
     * @param serialized the serialized PerfectLinkMessage
     * @return the PerfectLinkMessage corresponding to the byte array
     */
    public static PerfectLinkMessage deserialize(byte[] serialized) {
        PerfectLinkMessage plm = new PerfectLinkMessage();

        if (serialized[0] == 0) {
            plm.type = MessageType.ACK;
        } else {
            plm.type = MessageType.MESSAGE;
        }

        byte[] temp = Arrays.copyOfRange(serialized, 1, 5);
        plm.seq = ByteBuffer.wrap(temp).getInt();
        plm.hash = plm.seq;

        if (plm.type == MessageType.MESSAGE) {
            

            temp = Arrays.copyOfRange(serialized, 5, 9);
            int origin = ByteBuffer.wrap(temp).getInt();

            temp = Arrays.copyOfRange(serialized, 9, 13);
            int sender = ByteBuffer.wrap(temp).getInt();

            byte[] data =Arrays.copyOfRange(serialized, 13, serialized.length);
            
            Message m = new Message(origin, sender, data);

            plm.message = m;

            plm.hash += m.hashCode();
        }

        return plm;
    }

    /**
     * Concatenates 2 byte arrays. Both inputs cannot be null.
     *
     * @param arr1
     * @param arr2
     * @return
     */
    private static byte[] concatenate(byte[] arr1, byte[] arr2) {
        if (arr1 == null && arr2 == null) {
            throw new NullPointerException("Both inputs cannot be null");
        } else if (arr1 == null) {
            return arr2;
        } else if (arr2 == null) {
            return arr1;
        }

        byte[] result = new byte[arr1.length + arr2.length];

        int i;
        for (i = 0; i < arr1.length; i++) {
            result[i] = arr1[i];
        }

        for (int j = 0; j < arr2.length; j++) {
            result[i + j] = arr2[j];
        }

        return result;
    }

    public MessageType getType() {
        return type;
    }

    public Message getMessage() {
        return message;
    }

    public int getSeq() {
        return seq;
    }

    public PerfectLinkMessage getACKfromMessage() {
        if (type == MessageType.MESSAGE) {
            return new PerfectLinkMessage(MessageType.ACK, seq, null);
        } else {
            throw new IllegalStateException("Cannot transform an ACK");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof PerfectLinkMessage) {
            PerfectLinkMessage that = (PerfectLinkMessage) o;
            return this.type == that.type && this.message.equals(that.message) && this.seq == that.seq;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append(type).append("\n")
				.append(seq).append("\n")
				.append(message).append("\n")
				.append(hash).toString();
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
