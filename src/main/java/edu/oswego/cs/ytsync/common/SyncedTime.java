package edu.oswego.cs.ytsync.common;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;

public class SyncedTime {

	private InetAddress ntpServer;
	private long timeDifference = 0;

	public SyncedTime(String ntpServerString) {
		try {
			InetAddress ipAddr = InetAddress.getByName(ntpServerString);
			this.ntpServer = ipAddr;
		} catch (Exception e) {
			System.out.println("Error: Unrecognized NTP Server!");
			e.printStackTrace();
		}

		this.updateTime();
	}

	public void updateTime() {
		try {
			NTPUDPClient ntpClient = new NTPUDPClient();
			TimeInfo ntpInfo = ntpClient.getTime(this.ntpServer);
			long returnTime = ntpInfo.getReturnTime();
			this.timeDifference = returnTime - System.currentTimeMillis();
		} catch (Exception e) {
			System.out.println("Error: Problem While Fetching Time!");
			e.printStackTrace();
		}

	}

	public long getTime() {
		return System.currentTimeMillis() + timeDifference;
	}
}
