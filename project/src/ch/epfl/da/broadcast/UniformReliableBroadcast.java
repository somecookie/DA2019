package ch.epfl.da.broadcast;

import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import ch.epfl.da.Callback;
import ch.epfl.da.Process;
import ch.epfl.da.message.Message;

/**
 * This class implements a UniformReliableBroadcast on top of
 * {@link BestEffortBroadcast} using UDP For more information, see "Algorithm
 * 3.5: Majority-Ack Uniform Reliable Broadcast" in "Introduction to Reliable
 * and Secure Distributed Programming"
 */
public class UniformReliableBroadcast {

	private final Map<Message, Boolean> delivered; // There is no concurrent sets...
	private final Map<Message, Boolean> pending;
	private final Map<Message, Set<Integer>> ack;
	private final int processesMajority;
	private Process broadcaster;
	private Callback<Message> urbDeliver;
	private BestEffortBroadcast beb;

	/**
	 * Constructor of the class
	 *
	 * @param broadcaster the originator of the broadcast
	 * @param urbDeliver  the CallBack to use when a message is delivered
	 */
	public UniformReliableBroadcast(Process broadcaster, Callback<Message> urbDeliver) throws SocketException {
		if (broadcaster == null || broadcaster.getInetSocketAddress() == null || broadcaster.getPeers() == null) {
			throw new NullPointerException("The broadcaster cannot be null");
		}
		if (urbDeliver == null) {
			throw new NullPointerException("The callBack cannot be null");
		}

		this.broadcaster = broadcaster;
		this.urbDeliver = urbDeliver;

		delivered = new ConcurrentHashMap<>();
		pending = new ConcurrentHashMap<>();
		ack = new ConcurrentHashMap<>();

		processesMajority = (int) Math.ceil(broadcaster.getPeers().length / 2.0);

		beb = new BestEffortBroadcast(broadcaster, new Callback<Message>() {
			@Override
			public void onSuccess(Message message) {
				bebDeliver(message);
			}
		});
	}

	private void bebDeliver(Message message) {
		if (ack.containsKey(message)) {
			ack.get(message).add(message.getSenderPID());
		} else {
			Set<Integer> ackProcesses = new HashSet<>();
			ackProcesses.add(message.getSenderPID());
			ack.put(message, ackProcesses);
		}

		if (!pending.containsKey(message)) {
			pending.put(message, true);
			beb.broadcast(message.resend(broadcaster.getPID()));
		}

		if (canDeliver(message) && !delivered.containsKey(message)) {
			delivered.put(message, true);
			urbDeliver.onSuccess(message);
		}

	}

	/**
	 * Broadcast the message m using BestEffortBroadcast
	 *
	 * @param m the message that needs to be sent
	 */
	public void broadcast(Message m) {
		pending.put(m, true);
		beb.broadcast(m); // beb broadcast does not send it back to yourself, it beb delivers it directly
	}

	private boolean canDeliver(Message m) {
		return ack.get(m).size() >= processesMajority;
	}

}
