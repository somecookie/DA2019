package ch.epfl.da.message;

import java.util.Arrays;

/**
 * This abstract class represents the message that are broadcast.
 */
public class Message {
	private final int origin;
	private final int senderPID;
	private final byte[] data;
	private final int hash;

	public Message(int origin, int senderPID, byte[] data) {
		if (data == null) {
			throw new IllegalArgumentException("The arguments of Message cannot be null");
		}

		this.origin = origin;
		this.senderPID = senderPID;
		this.data = data;
		this.hash = 31 * Arrays.hashCode(data) + origin;

	}

	public int getOrigin() {
		return origin;
	}

	public int getSenderPID() {
		return senderPID;
	}

	public byte[] getData() {
		return data;
	}

	public Message resend(int senderPID) {
		return new Message(origin, senderPID, data.clone());
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("Message from sender: ").append(senderPID).append(" origin: (").append(origin).append(")");
		s.append(" with data: ");
		for (byte b : data) {
			s.append(b).append(" ");
		}
		s.append("and hash: ").append(hash);
		return s.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof Message) {
			Message that = (Message) o;
			return origin == that.origin && Arrays.equals(data, that.data);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}

}
