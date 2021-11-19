package ch.epfl.da.examples;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ClientUDP {

	public static void main(String[] args) throws IOException {
		byte[] buf = new byte[256];

		InetAddress address = InetAddress.getByName("127.0.0.1");
		int port = 12345;
		InetSocketAddress socketAddress = new InetSocketAddress(address, port);

		DatagramPacket packet = new DatagramPacket(buf, buf.length, socketAddress);
		DatagramSocket socket = new DatagramSocket(12346);
		socket.send(packet);
		socket.close();

	}
}
