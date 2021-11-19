package ch.epfl.da.message;

import java.math.BigInteger;

public class FIFOMessage implements Comparable<FIFOMessage> {
	private final Message message;
	private final int sequenceID;

	public FIFOMessage(Message message) {
		this.message = message;
		sequenceID = new BigInteger(message.getData()).intValue();
	}

	public Message message() {
		return message;
	}

	public int sequenceID() {
		return sequenceID;
	}

	@Override
	public int compareTo(FIFOMessage o) {
		return Integer.compare(sequenceID, o.sequenceID);
	}
}
