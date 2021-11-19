package ch.epfl.da.broadcast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import ch.epfl.da.Callback;
import ch.epfl.da.Process;
import ch.epfl.da.message.Message;
import ch.epfl.da.perfectLink.PerfectLink;

/**
 * This class implements a BestEffortBroadcast using UDP
 */
public class BestEffortBroadcast {

	private Process broadcaster;
	private PerfectLink link;
	private Callback<Message> bebDeliver;

	/**
	 * Constructor of the class
	 *
	 * @param broadcaster the originator of the broadcast
	 * @param bebDeliver  the CallBack to use when a message is delivered
	 */
	public BestEffortBroadcast(Process broadcaster, Callback<Message> bebDeliver) throws SocketException {
		if (broadcaster == null || broadcaster.getInetSocketAddress() == null) {
			throw new NullPointerException("The broadcaster cannot be null");
		}
		if (bebDeliver == null) {
			throw new NullPointerException("The CallBack cannot be null");
		}

		this.broadcaster = broadcaster;
		this.bebDeliver = bebDeliver;

		// the callBack of the perfect link is equivalent to deliver in
		// BestEffortBroadcast
		link = new PerfectLink(broadcaster.getInetSocketAddress(), bebDeliver, broadcaster);
		link.start();
	}

	/**
	 * Broadcast the message m to all peers of broadcaster
	 *
	 * @param message the message that needs to be broadcast
	 */
	public void broadcast(Message message) {
		bebDeliver.onSuccess(message);
		for (InetSocketAddress target : broadcaster.getOthers()) {
			try {
				link.send(message, target);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
