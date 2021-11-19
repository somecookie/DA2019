package ch.epfl.da.broadcast;

import java.math.BigInteger;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicIntegerArray;

import ch.epfl.da.Callback;
import ch.epfl.da.Process;
import ch.epfl.da.message.FIFOMessage;
import ch.epfl.da.message.Message;

/**
 * Implements FIFO broadcast on top of {@link UniformReliableBroadcast}.
 *
 */
public class FIFOBroadcast {
	private final UniformReliableBroadcast urb;
	private final ArrayList<ConcurrentSkipListSet<FIFOMessage>> pending;
	private final AtomicIntegerArray seq;
	private final Callback<FIFOMessage> fifoDeliver;
	
	/**
	 * Initialize FIFOBroadcast.
	 * @param process the process that broadcast the message
	 * @param fifoDeliver the method to call when delivering messages
	 * @throws SocketException
	 */
	public FIFOBroadcast(Process process, Callback<FIFOMessage> fifoDeliver) throws SocketException {

		this.fifoDeliver = fifoDeliver;
		int nbrProcesses = process.getPeers().length;
		pending = new ArrayList<>(nbrProcesses);
		for (int i = 0; i < nbrProcesses; ++i) {
			pending.add(i, new ConcurrentSkipListSet<>());
		}

		seq = new AtomicIntegerArray(nbrProcesses);

		urb = new UniformReliableBroadcast(process, new Callback<Message>() {

			@Override
			public void onSuccess(Message value) {
				urbDeliver(value);
			}
		});
	}

	/**
	 * Broadcast a message m to all process
	 * @param m a message
	 */
	public void broadcast(Message m) {
		urb.broadcast(m);
	}

	/**
	 * Deliver a message
	 * @param message the message to deliver
	 */
	private void urbDeliver(Message message) {
		int id = new BigInteger(message.getData()).intValue();
		int pid = message.getOrigin() - 1;
		int curId = seq.get(pid);
		if (curId == (id - 1)) {
			fifoDeliver.onSuccess(new FIFOMessage(message));
			++curId;
			//Deliver message for process pid
			Iterator<FIFOMessage> it = pending.get(pid).iterator();
			while (it.hasNext()) {
				FIFOMessage m = it.next();
				if (m.sequenceID() -1 == curId) {
					fifoDeliver.onSuccess(m);
					++curId;
					it.remove();
				} else {
					break; // The set is sorted
				}
			}
			seq.set(pid, curId);
		} else {
			pending.get(pid).add(new FIFOMessage(message));
		}
	}

}
