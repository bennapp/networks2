import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.sql.Timestamp;

public class TCPSender implements Runnable{
    public InetAddress iPAddress;
    public String iPStringAddress;
    public short portNumDest;
    public short portNumSource;
    public String fileName;
    public int nextSeqNum;
    public int windowSize;
    public PrintWriter writer;
    public static int base;
    public static byte[][] packets;
    public static boolean[] windowAck;
    public static DatagramSocket ackSocket;
    public static long[] startTime;
    public static long[] endTime;
    public static long[] diffTime;
    public static long timer;
    public static long timerOut = 50;
    public static long sum = 0;
    public static String options = "";
    public int bytesRecv;
    public int bytesSent;
    public int segSent;
    public int uniqSegSent;

	public TCPSender(String iPStringAddress, int portNumDestIn, int portNumSourceIn, String fileNameIn, int windowSizeIn, String logFileName, String optionsIn){ //probably will take in a file name also
		try{
		options = optionsIn;
		if(portNumSourceIn < 1023 || portNumDestIn < 1023 || portNumSourceIn > 49151 || portNumDestIn > 49151){
			System.err.println("Invalid port number(s), prort number must be 1023 < portNum < 49151");
			System.exit(0);
		} else {
			portNumSource = (short)portNumSourceIn;
			portNumDest = (short)portNumDestIn;
		}
		timerOut = 50; //to avoid hardcoding penalty in millis
		writer = new PrintWriter(logFileName);
		windowAck = new boolean[windowSizeIn];
		initAckWindow(windowAck, windowSizeIn);
		windowSize = windowSizeIn;
		fileName = fileNameIn;
		iPStringAddress = iPStringAddress;
		iPAddress = InetAddress.getByName(iPStringAddress);
		ackSocket = new DatagramSocket(portNumSource); 
		} catch (Exception e){
			e.printStackTrace();
		}

		packets = Packet.createPackets(portNumSource, portNumDest, windowSize, fileName);

		startTime = new long[packets.length];
		endTime = new long[packets.length];
		diffTime = new long[packets.length];

		base = 0;
		nextSeqNum = 0;

		new Thread(new TCPSender(true)).start(); //begin receiving acks
		
		boolean[] sent = new boolean[packets.length];

		while(true){
			try{
				nextSeqNum = base;
				if(base < packets.length){
					if(timeOut()){ //Send out packets within window size that have not been ack'ed yet
						while(nextSeqNum < base + windowSize && nextSeqNum < packets.length && nextSeqNum >= base){ //because it is here
							if(windowAck[nextSeqNum - base] == false ){ // window[i] does not make sense
      							if(options.equals("windowSize")){
      								System.out.println("base = " + base);
      								System.out.println("windowSize = " + windowSize);
      								System.out.println("Segment sent = " + nextSeqNum);
      								System.out.println("");
      							}
    							Packet packet = new Packet(packets[nextSeqNum]);
    							DatagramPacket sendPacket = new DatagramPacket(packet.bytes, packet.bytes.length, iPAddress, portNumDest);
      							ackSocket.send(sendPacket);
      							if(sent[nextSeqNum] == false){
      								uniqSegSent += 1;
      								startTime[nextSeqNum] = System.currentTimeMillis();
      								sent[nextSeqNum] = true;
      							}
								java.util.Date date = new java.util.Date();
	 							String dateString = (new Timestamp(date.getTime())).toString();
								String logLine = dateString + ", " + InetAddress.getByName(iPStringAddress) + ", Source: " +  InetAddress.getLocalHost().getHostAddress() + ", SequenceNum: ";
								logLine = logLine + packet.sequenceNum + ", ACKNum: " + packet.ackNum + ", ackF: " + packet.ack + ", finF: " + packet.fin + "\n"; //sequence num, acknum, flags
								writer.println(logLine);
								bytesSent += packet.bytes.length;
								segSent += 1;
							}
							startTimer();
      						nextSeqNum++;
						}
					}
				} else { //wrap up program dish out stats
					System.out.println("Delivery completed successfully");
					bytesRecv = packets.length * packets[packets.length-1].length;
					System.out.println("Total bytes sent = " + bytesSent);
					System.out.println("Total bytes received = " + bytesRecv);
					System.out.println("Segments sent = " + segSent);
					System.out.println("Segments retransmitted = " + (segSent - uniqSegSent) );

					long averageTime = (sum/endTime.length);
					System.out.println("Average RTT = " + averageTime + " milliseconds");
					writer.close();
					System.exit(0);
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public TCPSender(boolean noInit){ //prevent recursively creating threads
	}

	public void initAckWindow(boolean[] ackWindow, int windowSize){
		for(int i = 0; i < windowSize; i++){
			ackWindow[i] = false;
		}
	}

	public static int shiftWindowCheck(){ //see how much ack window should be increased can properly return 0 if no shift needed
		int count = 0;
		for(int i = 0; i < windowAck.length; i++){
			if(windowAck[i] == true){
				count++;
			} else {
				break;
			}
		}
		return count;
	}

	public static void shiftWindow(int shift){ //inc ack window
		boolean temp;
		for(int i = 0; i < windowAck.length; i++){
			if(i + shift < windowAck.length){
				temp = windowAck[i + shift];
				windowAck[i] = temp;
			}
		}
		for(int i = windowAck.length-1; i > windowAck.length - 1 - shift; i--){
			windowAck[i] = false;
		}
	}

    public static void startTimer(){
    	timer = System.currentTimeMillis();
    }

    public static boolean timeOut(){ //simple timout
    	long time = System.currentTimeMillis();
    	boolean timedOut = false;
    	if((time - timer) > timerOut){
    		timedOut = true;
    	}
    	return timedOut;
    }

    public void adjTimer(long rTT){ //simple timer adj
    	timer = (long)((long)0.25 * timer) + (long)((long)0.75 * rTT);
    }

	public void run(){
		while(true){ //receive acks
			try{
				int basePrev = base;
				while(basePrev - base == 0){//base has not changed
					byte[] receiveACKData = new byte[4];
					DatagramPacket receiveACKPacket = new DatagramPacket(receiveACKData, receiveACKData.length);
 					ackSocket.receive(receiveACKPacket);
 					int ack = Packet.getInt(receiveACKData, 0);
					if(ack >= base){
						if(windowAck[ack-base] == false){
							if(options.equals("windowSize")){
								System.out.println("---------------");
								System.out.println("Recieved ack " + ack);
								System.out.println("---------------");
							}
							endTime[ack] = System.currentTimeMillis();
							diffTime[ack] = endTime[ack] - startTime[ack];
							adjTimer(diffTime[ack]);
							sum += diffTime[ack];
						}
						windowAck[ack - base] = true;
					}
					int shiftWindowCount = shiftWindowCheck();
					shiftWindow(shiftWindowCount);
					base += shiftWindowCount;
					if(base < packets.length){
						startTimer();
					}
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}