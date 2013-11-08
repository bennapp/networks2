import java.io.*;
import java.net.*;

public class Receiver{

	public static void main(String[] args){
		String fileRead = "";
		int listeningPort = 0;
		String remoteIP = "";
		int remotePort = 0;
		String fileLog = "";


		try{
			//fileRead = args[0];
			//listeningPort = Integer.parseInt(args[1]);
			//remoteIP = args[2];
			//remotePort = Integer.parseInt(args[3]);
			//fileLog = args[4];
			//check if ports are valid
			
			fileRead = "readFileTest.txt";
			fileLog = "fileLogTest.txt";
			listeningPort = Integer.parseInt(args[0]);
			remoteIP = "localhost";
			remotePort = 7000;
			
			TCPReceiver tCPR = new TCPReceiver(fileRead, listeningPort, remoteIP, remotePort, fileLog);

		} catch (Exception e){
			System.err.println(e);
		}

	} 
//receiver file.txt 20000 128.59.15.37 20001 logfile.txt
//command line exec with filename, listening_port remote_IP, remote_port,
//log_filename
}