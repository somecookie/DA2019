package ch.epfl.da.perfectLink;

public class MessageID {

	private final int pid;
	private final int seq;

	public MessageID(int pid, int seq) {
		this.pid = pid;
		this.seq = seq;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof MessageID) {
			MessageID that = (MessageID) o;
			return that.pid == this.pid && that.seq == this.seq;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return pid * 99991 + seq;
	}
}
