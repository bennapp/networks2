import java.io.*;
import java.net.*;

public class Sender /*implements Runnable*/{
	public static TCPSender tCPSender;

	public static void main(String[] args){
		//error check on each of these inputs

		//file
		try{
			//open file
			//check if reasonable port number
			//check if resonable port number
			
			//String fileRead = args[0];
			//String remoteIP = args[1];
			//int remotePort = 	Integer.parseInt(args[2]);
			// int ackPortNumber = Integer.parseInt(args[3]);
			//int windowSize = 	Integer.parseInt(args[4]);
			//String fileLog = args[5];
			
			String remoteIP = args[0];
			int remotePort = Integer.parseInt(args[1]);
			int ackPortNumber = 9999;
			String fileName = "file.txt";
			int windowSize = 10;
			String logFileName = "logfileSender.txt";

		tCPSender = new TCPSender(remoteIP, remotePort, ackPortNumber, fileName, windowSize, logFileName);
		tCPSender.dataRecv();

		} catch(Exception e){
			System.err.println(e);
		}


		//sender file.txt 128.59.15.38 20000 20001 1152 logfile.txt
		//command line exec with filename, remote_IP, remote_port, ack_port_number,
		//window_size, log_filename

	}

	//public void run(){
	//	System.out.println("I was called");
	//}
}