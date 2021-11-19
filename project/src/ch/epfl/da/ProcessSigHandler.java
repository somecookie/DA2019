package ch.epfl.da;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.epfl.da.io.Logger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * Class which handles signals (USR2, TERM, INT)
 */
public class ProcessSigHandler extends Thread {
	private final Logger logger;
	private AtomicBoolean waitForStart;

	/**
	 * Constructs a signal handler
	 * @param logger the logger in which messages broadcast and delivered
	 *                  by the process are written to
	 */
	public ProcessSigHandler(Logger logger) {
		SigHandlerUsr2 sigHandlerUsr2 = new SigHandlerUsr2(this);
		SigHandlerInt sigHandlerTerm = new SigHandlerInt(this);
		SigHandlerTerm sigHandlerInt = new SigHandlerTerm(this);

		Signal signalTerm = new Signal("TERM");
		Signal signalInt = new Signal("INT");
		Signal signalUsr2 = new Signal("USR2");

		Signal.handle(signalInt, sigHandlerInt);
		Signal.handle(signalTerm, sigHandlerTerm);
		Signal.handle(signalUsr2, sigHandlerUsr2);

		this.logger = logger;
		waitForStart = new AtomicBoolean(true);
		this.start();
	}

	/**
	 * @return the logger in which messages broadcast and delivered
	 * by the process are written to
	 */
	private Logger getLogger() {
		return logger;
	}

	/**
	 * @return false if the signal handler received a USR2 signal (starting signal),
	 * true otherwise
	 */
	AtomicBoolean waitForStart() {
		return waitForStart;
	}

	/**
	 * Set the waitForStart atomic boolean to false,
	 * meaning the process can start broadcasting
	 */
	private void setWaitForStart() {
		waitForStart.set(false);
	}

	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				// exception
			}
		}
	}

	/**
	 * Class which handles USR2 signals (starting signals)
	 */
	@SuppressWarnings("deprecation")
	public static class SigHandlerUsr2 implements SignalHandler {
		ProcessSigHandler p;

		private SigHandlerUsr2(ProcessSigHandler p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			p.setWaitForStart();
		}
	}

	/**
	 * Class which handles TERM signal (killing signals)
	 */
	@SuppressWarnings("deprecation")
	public static class SigHandlerTerm implements SignalHandler {
		ProcessSigHandler p;

		private SigHandlerTerm(ProcessSigHandler p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			try {
				p.getLogger().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(-1);
		}
	}

	/**
	 * Class which handles INT signal (killing signals)
	 */
	@SuppressWarnings("deprecation")
	public static class SigHandlerInt implements SignalHandler {
		ProcessSigHandler p;

		private SigHandlerInt(ProcessSigHandler p) {
			super();
			this.p = p;
		}

		@Override
		public void handle(Signal signal) {
			try {
				p.getLogger().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(-1);
		}
	}
}
