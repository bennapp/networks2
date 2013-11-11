import java.io.*;
import java.net.*;

public class Receiver{
	public static void main(String[] args){
		try{
			if(!(args.length == 5) && !(args.length == 6)){
				System.out.println(args.length);
				System.err.println("Invalid args length");
				System.err.println("Try: java Receiver file.txt 4119 localhost 9999 logRecFile.txt");
				System.exit(0);
			}
			
			String fileRead = args[0];
			int listeningPort = Integer.parseInt(args[1]);
			String remoteIP = args[2];
			int remotePort = Integer.parseInt(args[3]);
			String fileLog = args[4];
			String options = "";

			if(args.length == 6){
				options = args[5];
			}
			TCPReceiver tCPR = new TCPReceiver(fileRead, listeningPort, remoteIP, remotePort, fileLog, options);
		} catch (Exception e){
			System.err.println(e);
		}

	} 

}