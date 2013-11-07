import java.io.*;
import java.net.*;
import java.lang.*;

public class TCPReceiver{
	public String fileRead;
	public int listeningPort;
	public String remoteIP;
	public int remotePort;
	public String fileLog;

	public TCPReceiver(String fileRead, int listeningPort, String remoteIP, int remotePort, String fileLog){
		this.fileRead = fileRead;
		this.listeningPort = listeningPort;
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.fileLog = fileLog;
	
  		try{

		DatagramSocket serverSocket = new DatagramSocket(listeningPort); 
  		byte[] receiveData = new byte[1024]; 

		while(true){
  			//System.out.println("Starting server " + dsock.getPort());
  			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length );
  			serverSocket.receive(receivePacket);
  			String sentance = new String(receivePacket.getData());
  			System.out.println("Received: " + sentance);   

  			//InetAddress IPAddress = receivePacket.getAddress();
            //int port = receivePacket.getPort();
            //String capitalizedSentence = sentence.toUpperCase();
            //sendData = capitalizedSentence.getBytes();
            //DatagramPacket sendPacket =
            //new DatagramPacket(sendData, sendData.length, IPAddress, port);
            //serverSocket.send(sendPacket);
		}
  		} catch (Exception e){
  			System.out.println(e);
  		}   
	}
	public static void main(String[] args){
	}
}