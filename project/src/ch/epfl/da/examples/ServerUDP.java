package ch.epfl.da.examples;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ServerUDP {

	public static void main(String[] args) throws IOException {
		byte[] buf = new byte[256];
		DatagramSocket socket = new DatagramSocket(12345);

		while (true) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			socket.receive(packet);
			System.out.println(packet.getAddress().toString() + " " + buf[0]);
		}
	}
}
