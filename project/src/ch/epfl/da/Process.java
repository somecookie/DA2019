package ch.epfl.da;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Class that represents a process with its ID,
 * socket address (IP and port), lists of its peer processes,
 * and for Localized Causal Broadcast, a list of which process affects it
 */
public class Process implements Serializable {
    private int pID;
    private InetSocketAddress inetSocketAddress;
    private int[] affectedBy;
    private InetSocketAddress[] peers;
    private InetSocketAddress[] others;

    /**
     * Construct a process from its ID and socket address
     *
     * @param pID               the ID of the process
     * @param inetSocketAddress the socket address (IP and port) of the process
     */
    public Process(int pID, InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            throw new NullPointerException("The InetSocketAddress of the process cannot be null");
        }

        this.pID = pID;
        this.inetSocketAddress = inetSocketAddress;
        this.affectedBy = null;
        this.peers = null;
        this.others = null;
    }

    /**
     * @return the ID of the process
     */
    public int getPID() {
        return pID;
    }

    /**
     * @return the socket address (IP and port) of the process
     */
    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    /**
     * For Localized Causal Broadcast
     *
     * @return a list containing the IDs of the processes that can affect this one
     */
    public int[] getAffectedBy() {
        return affectedBy;
    }

    /**
     * For Localized Causal Broadcast
     *
     * @param affectedBy the list containing the IDs
     *                   of the processes that can affect this one
     */
    public void setAffectedBy(int[] affectedBy) {
        this.affectedBy = affectedBy;
    }

    /**
     * @return the socket addresses (IP and port) of all the processes
     * appearing in the membership file
     */
    public InetSocketAddress[] getPeers() {
        return peers;
    }

    /**
     * Set the peers (all the processes in the membership files)
     * and the others (all the peers except this process)
     *
     * @param peers the list of all the socket addresses (IP and port)
     *              in the membership file
     */
    public void setPeersAndOthers(InetSocketAddress[] peers) {
        this.peers = peers;
        this.others = new InetSocketAddress[peers.length - 1];

        int i = 0;
        for (InetSocketAddress p : peers) {
            if (p != inetSocketAddress) {
                others[i] = p;
                i += 1;
            }
        }

    }

    /**
     * @return the socket addresses (IP and port) of all the processes
     * appearing in the membership file except this one
     */
    public InetSocketAddress[] getOthers() {
        return others;
    }

    /**
     * Get the ID of a process from its socket address
     *
     * @param addr the socket address (IP and port) of the wanted process
     * @return the ID of the wanted process
     */
    public int pidFromAddr(InetSocketAddress addr) {
        for (int i = 0; i < peers.length; ++i) {
            if (addr.equals(peers[i])) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("The socket address" + addr.toString() + " is not known");
    }

    @Override
    public boolean equals(Object that) {
        if (this == that)
            return true;

        if (that instanceof Process) {
            Process thatProcess = (Process) that;
            return pID == thatProcess.pID && Arrays.equals(affectedBy, thatProcess.affectedBy)
                    && Arrays.equals(peers, thatProcess.peers)
                    && inetSocketAddress.equals(thatProcess.inetSocketAddress);
        } else {
            return false;
        }
    }
}
