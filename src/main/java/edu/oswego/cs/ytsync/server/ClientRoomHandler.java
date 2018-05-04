package edu.oswego.cs.ytsync.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ClientRoomHandler extends Thread {
	final DataOutputStream dout;
	final DataInputStream din;
	final ServerSocket roomServer;
	final int roomNum;
	final int serverPort;
	final Socket socket;

	public ClientRoomHandler(int roomNumber) throws IOException {
		this.roomServer = new ServerSocket(0);
		this.serverPort = roomServer.getLocalPort();
		this.roomNum = roomNumber;
		this.socket = roomServer.accept();
		this.dout = new DataOutputStream(socket.getOutputStream());
		this.din = new DataInputStream(socket.getInputStream());
	}
}
