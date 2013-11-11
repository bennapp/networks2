import java.io.*;
import java.net.*;
import java.lang.*;
import java.sql.Timestamp;
import java.util.*;

public class TCPReceiver{
	public String fileRead;
	public int listeningPort;
	public String remoteIP;
	public int remotePort;
	public String fileLog;
	public int maxSequenceNum;
	public boolean[] packetRecv;
	public boolean finFound;
	public TreeSet<Packet> incPackets = new TreeSet<Packet>();
	public String options = "";

	public TCPReceiver(String fileRead, int listeningPort, String remoteIP, int remotePort, String fileLog, String options){
		this.options = options;
		if(listeningPort < 1023 || remotePort < 1023 || listeningPort > 49151 || remotePort > 49151){
			System.err.println("Invalid port number(s), prort number must be 1023 < portNum < 49151");
			System.exit(0);
		} else {
			this.remotePort = remotePort;
			this.listeningPort = listeningPort;
		}

		this.fileRead = fileRead;
		this.remoteIP = remoteIP;
		this.fileLog = fileLog;
		this.maxSequenceNum = 0;
		finFound = false;
		
		PrintWriter writer;
		FileOutputStream fileWriter;
		packetRecv = new boolean[10000000]; //5Gigabyte max capacity should be sufficient
  		
  		try{
		DatagramSocket serverSocket = new DatagramSocket(listeningPort); 
  		byte[] receiveData = new byte[576]; 
  		writer = new PrintWriter(fileLog);
  		fileWriter = new FileOutputStream(new File(fileRead));

		while(true){
  			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
  			serverSocket.receive(receivePacket);

  			Packet packet = new Packet(receivePacket.getData());

  			short checkSum = Packet.checkSum(packet.bytes);
  			boolean validCheckSum = false;
  			short packetCheckSum = Packet.getShort(packet.bytes, 16);
  			
  			if(checkSum == packetCheckSum){
  				validCheckSum = true;
  			}

  			if(packetRecv[packet.ackNum] == false && validCheckSum){
  				//writing to file
  				byte[] data = Packet.fetchData(packet.bytes, packet.dataLength);

  				//store the data to be sorted
  				incPackets.add(new Packet(packet.bytes));
  				packetRecv[packet.ackNum] = true;
  			
  				//sending ack
            	byte[] sendAck = new byte[4];
            	Packet.setInt(sendAck, 0, packet.ackNum);
            	if(options.equals("outOfOrder")){
					System.out.println("ackNum " + packet.ackNum);
            	}

	            DatagramPacket sendPacket = new DatagramPacket(sendAck, sendAck.length, InetAddress.getByName(remoteIP), remotePort);
            	serverSocket.send(sendPacket);
  				
  				if(packet.fin == 1){
					finFound = true;
					maxSequenceNum = packet.ackNum;
				}
  			}
  			//writing to log
  			java.util.Date date = new java.util.Date();
	 		String dateString = (new Timestamp(date.getTime())).toString();
			String logLine = dateString + ", " + InetAddress.getByName(remoteIP) + ", Source: " +  InetAddress.getLocalHost().getHostAddress() + ", SequenceNum: ";
			logLine = logLine + packet.sequenceNum + ", ACKNum: " + packet.ackNum + ", ackF: " + packet.ack + ", finF: " + packet.fin + "\n"; //sequence num, acknum, flags
			
			if(fileLog.equals("stdout")){
				System.out.println(logLine);
			} else {
				writer.println(logLine);
			}

            if(finFound && checkFinished()){
            	break;
            }
		}

		Iterator<Packet> i = incPackets.iterator();
		while(i.hasNext()){
			Packet packet = i.next();
			fileWriter.write(packet.getData());
		}

		System.out.println("Received " + fileRead);
		writer.close();
		fileWriter.close();
  		} catch (Exception e){
  			System.out.println(e);
  		}   
	}

	public boolean checkFinished(){
		boolean finished = true;
		for(int i=0; i < maxSequenceNum; i++){
			if(packetRecv[i] == false){
				finished = false;
			}
		}
		return finished;	
	}
}