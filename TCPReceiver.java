import java.io.*;
import java.net.*;
import java.lang.*;
import java.sql.Timestamp;
import java.util.Date;

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
		
		PrintWriter writer;
		FileOutputStream fileWriter;
		boolean[] sequenceNums = new boolean[10000000]; //5Gigabyte
		String sentance ="";

  		try{

		DatagramSocket serverSocket = new DatagramSocket(listeningPort); 
  		byte[] receiveData = new byte[576]; 
  		writer = new PrintWriter(fileLog);
  		fileWriter = new FileOutputStream(new File (fileRead));



		while(true){

  			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
  			serverSocket.receive(receivePacket);
  			byte[] packet = receivePacket.getData();
  			int sequenceNum = (int)( ((packet[4]&0xFF)<<24) | ((packet[5]&0xFF) << 16) | ((packet[6]&0xFF)<<8) | (packet[7]&0xFF) );
  			int countSeq = sequenceNum / 556;
  			if(sequenceNums[countSeq] == false){
  				//writing to file
  				short dataLength = (short)( ((packet[18]&0xFF)<<8) | (packet[19]&0xFF) );

  				byte[] data = new byte[dataLength];
  				int i = 0;
  				while(i<dataLength){
  					data[i] = packet[i + 20];
  					i++;
  				}
  				fileWriter.write(data);
  				sequenceNums[countSeq] = true;
  				//sentance = new String(data);
  				//System.out.println(sentance);
  			}


  			//writing to log
  			int ackNum = (int)( ((packet[8]&0xFF)<<24) | ((packet[9]&0xFF) << 16) | ((packet[10]&0xFF)<<8) | (packet[11]&0xFF) );
  			int ack = (int)(packet[12]&0xFF);
  			int fin = (int)(packet[13]&0xFF);

  			java.util.Date date = new java.util.Date();
	 		String dateString = (new Timestamp(date.getTime())).toString();
			String logLine = dateString + ", " + "remoteIP" + ", Source: " +  InetAddress.getLocalHost().getHostAddress() + ", SequenceNum: ";
			logLine = logLine + sequenceNum + ", ACKNum: " + ackNum + ", ackF: " + ack + ", finF: " + fin + "\n"; //sequence num, acknum, flags
			writer.println(logLine);

			//sending ack
            byte[] sendAck = new byte[4];
            sendAck[0] = (byte) (ackNum >> 24);
            sendAck[1] = (byte) (ackNum >> 16);
            sendAck[2] = (byte) (ackNum >> 8);
            sendAck[3] = (byte) ackNum;

            DatagramPacket sendPacket = new DatagramPacket(sendAck, sendAck.length, InetAddress.getByName(remoteIP), remotePort);
            serverSocket.send(sendPacket);
            if(fin == 1){//change this to 10 for testing
            	break;
            }
		}
			System.out.println("Received " + fileRead);
			writer.close();
			fileWriter.close();
  		} catch (Exception e){
  			System.out.println(e);
  		}   
	}
	public static void main(String[] args){
	}
}