import java.io.*;
import java.net.*;

public class Sender {
	public static TCPSender tCPSender;
	public static void main(String[] args){
		try{
			if(!(args.length == 6) && !(args.length == 7)){
				System.err.println("Invalid args length");
				System.err.println("Try: Sender file.txt localhost 5000 9999 10 logSendFile.txt");
				System.exit(0);
			}

			String fileRead = args[0];
			String remoteIP = args[1];
			int remotePort = 	Integer.parseInt(args[2]);
			int ackPortNumber = Integer.parseInt(args[3]);
			int windowSize = 	Integer.parseInt(args[4]);
			String fileLog = args[5];
			String options = "";

			if(args.length == 7){
				options = args[6];
			}
		tCPSender = new TCPSender(remoteIP, remotePort, ackPortNumber, fileRead, windowSize, fileLog, options);
		} catch(Exception e) {
			System.err.println(e);
		}
	}
}