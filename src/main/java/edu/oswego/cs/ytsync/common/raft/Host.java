package edu.oswego.cs.ytsync.common.raft;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {
    private String hostname;
    private InetAddress address;
    private int socketPort;

    public Host(String hostname, int socketPort) {
        this.hostname = hostname;
        try {
            address = InetAddress.getByName(this.hostname);
        } catch(UnknownHostException e) {
            System.out.println("Hostname not found.");
        }
        this.socketPort = socketPort;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getServerSocketPort() {
        return socketPort;
    }

    public String getHostname() {
        return hostname;
    }
}
