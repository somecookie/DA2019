package ch.epfl.da.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Logger class to write in a file the messages broadcast and delivered
 */
public final class Logger {

    private final BufferedWriter writer;

    /**
     * Creates a logger which will write in a file da_proc_"n".out
     *
     * @param n the id of the process
     * @throws IOException if there is a problem when creating the logger
     */
    public Logger(int n) throws IOException {
        this.writer = new BufferedWriter(new FileWriter("da_proc_" + n + ".out"));
    }

    /**
     * Writes b "seqNbr" in the file
     *
     * @param seqNbr the sequence number of the message
     * @throws IOException if it is not possible to write in the file
     */
    public void broadcast(int seqNbr) throws IOException {
        writer.append("b " + seqNbr + " \n");
    }

    /**
     * Writes d "sender" "seqNbr" in the file
     *
     * @param sender the id of the process whose message we deliver
     * @param seqNbr the sequence number of the message
     * @throws IOException if it is not possible to write in the file
     */
    public void deliver(int sender, int seqNbr) throws IOException {
        writer.append("d " + sender + " " + seqNbr + " \n");
    }

    /**
     * Close the logger
     *
     * @throws IOException if it is not possible to close the writer
     */
    public void close() throws IOException {
        writer.close();
    }

}
