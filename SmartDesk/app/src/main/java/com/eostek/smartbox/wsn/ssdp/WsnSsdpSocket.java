package com.eostek.smartbox.wsn.ssdp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class WsnSsdpSocket {
	 
    private MulticastSocket multicastSocket;
    private InetAddress inetAddress;
 
    public WsnSsdpSocket() throws IOException {
    	
    	close();
        //默认地址和端口：port： 1900,  address：239.255.255.250
        multicastSocket = new MulticastSocket(WsnSsdpConstants.PORT); // Bind some random port for receiving datagram
        inetAddress = InetAddress.getByName(WsnSsdpConstants.ADDRESS);
        multicastSocket.joinGroup(inetAddress);
    }
 
    /* Used to send SSDP packet */
    public void send(String data) throws IOException {
        DatagramPacket dp = new DatagramPacket(data.getBytes(), data.length(), inetAddress, WsnSsdpConstants.PORT);
        multicastSocket.send(dp);
    }
 
    /* Used to receive SSDP packet */
    public DatagramPacket receive() throws SocketTimeoutException,IOException  {
        byte[] buf = new byte[1024];
        DatagramPacket dp = new DatagramPacket(buf, buf.length);
        multicastSocket.setSoTimeout(15*1000);
        multicastSocket.receive(dp);
        return dp;
    }
 
    public void close() {
        if (multicastSocket != null && multicastSocket.isConnected()) {
            multicastSocket.close();
        }
    }
}
