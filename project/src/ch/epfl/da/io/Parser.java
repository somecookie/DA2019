package ch.epfl.da.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import ch.epfl.da.Process;

/**
 * Parser interface which parse the command line arguments to start a process and membership
 */
public interface Parser {

	/**
	 * Parse the processes in the file @arg
	 *
	 * @param pID the ID of the process which does the parsing
	 * @param arg the name of the file containing the processes
	 * @return a list of process from the file @arg
	 */
	static Process[] parseProcesses(int pID, String arg) {
		try {
			File file = new File(arg);
			BufferedReader reader = new BufferedReader(new FileReader(file));

			int numProcesses = Integer.parseInt(reader.readLine());
			Process[] processes = new Process[numProcesses];

			if (pID > numProcesses) {
				throw new IllegalArgumentException(
						"There are not enough processes in the files for process " + pID + " to exist.");
			}

			// Create all the processes
			for (int i = 0; i < numProcesses; i++) {
				String[] line = reader.readLine().split("\\s+");

				int id = Integer.parseInt(line[0]);
				InetAddress ipAddress = InetAddress.getByName(line[1]);

				int port = Integer.parseInt(line[2]);
				InetSocketAddress socket = new InetSocketAddress(ipAddress, port);

				processes[i] = new Process(id, socket);
			}

			// Handle the peers
			InetSocketAddress[] peers = new InetSocketAddress[numProcesses];
			for (int i = 0; i < numProcesses; i++) {
				peers[i] = processes[i].getInetSocketAddress();
			}

			for (Process p : processes) {
				p.setPeersAndOthers(peers);
			}

			// Handle the affectedBy list if we use Localized Causal Broadcast
			String line = reader.readLine();
			if (line != null) {
				for (int i = 0; i < numProcesses; i++) {
					String[] ints = line.split("\\s+");
					int id = Integer.parseInt(ints[0]);
					int[] affectedBy = new int[ints.length - 1];
					for (int j = 1; j < ints.length; j++) {
						affectedBy[j - 1] = Integer.parseInt(ints[j]);
					}
					processes[id - 1].setAffectedBy(affectedBy);
					line = reader.readLine();
				}
			}

			reader.close();
			return processes;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (IOException e) {
			throw new IllegalArgumentException("The file : " + arg + " does not comply to the formatting rules.");
		}
		finally {
			
		}
	}

	/**
	 * Parse the number of messages from the command line argument received
	 *
	 * @param arg the number of messages as a command line argument
	 * @return the number of messages as an int
	 */
	static int parseNumMessages(String arg) {
		return parseInt(arg, "The expected number of messages is not in the right format");
	}

	/**
	 * Parse the ID of the process from the command line argument received
	 *
	 * @param arg the ID of the process as a command line argument
	 * @return the ID of the process as an int
	 */
	static int parseID(String arg) {
		return parseInt(arg, "The expected ID of the process is not in the right format");
	}

	/**
	 * Parse an int from a given string
	 *
	 * @param arg   the given string to parse
	 * @param error the error to output if we could not transform the string
	 * @return the int parsed from @arg
	 */
	static int parseInt(String arg, String error) {
		try {
			return Integer.parseInt(arg);
		} catch (NumberFormatException e) {
			throw new NumberFormatException(error);
		}
	}
}
