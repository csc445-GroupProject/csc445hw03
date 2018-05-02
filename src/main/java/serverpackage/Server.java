package serverpackage;

import java.net.*;
import java.util.ArrayList;
import java.io.*;
import java.lang.*;

public class Server {
	
	private final static int port = 2706;
    
    public static void main(String args[]) throws IOException {
    	
		ServerSocket host = new ServerSocket(port);
		
		while(true) {
			int x = 0;
			Socket client = host.accept();
			DataInputStream dis = new DataInputStream(client.getInputStream());
			DataOutputStream dout = new DataOutputStream(client.getOutputStream());
			new Thread(() -> {
				ClientHandler conClient = new ClientHandler(client, dis, dout);
			}, "Client: " + x).start();
		}
    }
	   
}

class ClientHandler extends Thread{
	final DataOutputStream dout;
	final DataInputStream din;
	final Socket socket;
	
	public ClientHandler(Socket socket, DataInputStream din, DataOutputStream dout) {
		this.socket = socket;
		this.din = din;
		this.dout = dout;
	}
	
	
	@Override
	public void run() {
		while(true) {
			//Handle what client threads do
			//Take requests: add queue, remove queue, chat, leave and etc.
		}
	}
}




