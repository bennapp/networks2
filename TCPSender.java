import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.Scanner;

public class TCPSender implements Runnable{
	public static boolean dataRecv = false;
	public static boolean timeOut;
	public static boolean aCKRecv;
	public static int event = 0;
	public static DatagramSocket sendSocket;
    public static InetAddress iPAddress;
    public static String iPStringAddress;
    public static int portNumDest;
    public static int portNumSource;
    public static String fileName;
    public static int windowSize;
    public static int base;
    public static int nextSeqNum;
    public static byte[][] packets;
    public static boolean[] windowAck;
    public static DatagramSocket ackSocket;
    
    //file name also

	public TCPSender(){
		try{
			portNumDest = 8888;
			iPAddress = InetAddress.getByName("localhost");
			sendSocket = new DatagramSocket();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public TCPSender(String iPStringAddress, int portNumDestIn, int portNumSourceIn, String fileNameIn, int windowSizeIn){ //probably will take in a file name also
		try{
		windowAck = new boolean[windowSizeIn];
		initAckWindow(windowAck, windowSizeIn);
		windowSize = windowSizeIn;
		fileName = fileNameIn;
		portNumSource = portNumSourceIn;
		iPStringAddress = iPStringAddress;
		iPAddress = InetAddress.getByName(iPStringAddress);
		portNumDest = portNumDestIn;
		sendSocket = new DatagramSocket();
		ackSocket = new DatagramSocket(portNumSource); 
		} catch (Exception e){
			e.printStackTrace();
		}
		packets = createPackets();
		base = 0;
		nextSeqNum = 0;
		init(portNumDest);
	}

	public TCPSender(String iPStringAddress, int portNumDestIn, boolean noInit){ //prevent recursively creating threads
		try{
		iPStringAddress = iPStringAddress;
		iPAddress = InetAddress.getByName(iPStringAddress);
		portNumDest = portNumDestIn;
		sendSocket = new DatagramSocket();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void initAckWindow(boolean[] ackWindow, int windowSize){
		for(int i = 0; i < windowSize; i++){
			ackWindow[i] = false;
		}
	}

	public void init(int portNumDestIn){
		new Thread(new TCPSender(iPStringAddress, portNumDestIn, false)).start();
	}

	public void dataRecv(){
		dataRecv = true;
	}

	public int getEvent(){
		if(dataRecv){
			dataRecv = false;
			return 1;
		}
		if(timeOut){
			timeOut = false;
			return 2;
		}
		if(aCKRecv){
			aCKRecv = false;
			return 3;
		}
		return 0;
	}

	public static byte[][] createPackets(){
		byte[] allFileData = new byte[1];
		short sourcePort = (short)portNumSource;
		short destPort = (short)portNumDest;
		byte[] sourceDest = new byte[4];
		sourceDest[0] = (byte) (sourcePort >> 8);
		sourceDest[1] = (byte) sourcePort;
		sourceDest[2] = (byte) (destPort >> 8);
		sourceDest[3] = (byte) destPort;

		String fileString = "";
		try{
			File file = new File(fileName);
			allFileData = readFile(file);
			//Scanner scanner = new Scanner(file);
			//fileString = scanner.nextLine();
			//while (scanner.hasNextLine()) {
			//       fileString = fileString + "\n" + scanner.nextLine();
			//}
		} catch(Exception e){
			e.printStackTrace();
		}
		char[] charArray = fileString.toCharArray();
		//byte[] allFileData = new String(charArray).getBytes();
		int numSegments = (int) Math.ceil((double)allFileData.length / 556); //needs to round up
		int packetSize = 576;
		int checkSum;
		int sequenceNum = 0;

		byte[][] packets = new byte[numSegments][packetSize];
		for(int i = 0; i<numSegments; i++){
			int j = 0;
			while(j < 4){
				packets[i][j] = sourceDest[j]; j++;
			}
			sequenceNum = i * 556;
			packets[i][4] = (byte) (sequenceNum >> 24);
			packets[i][5] = (byte) (sequenceNum >> 16);
			packets[i][6] = (byte) (sequenceNum >> 8);
			packets[i][7] = (byte) (sequenceNum);
			int ackNum = i;
			packets[i][8] = (byte) 	(ackNum >> 24);
			packets[i][9] = (byte) (ackNum >> 16);
			packets[i][10] = (byte) (ackNum >> 8);
			packets[i][11] = (byte) (ackNum);

			int ack = 1;
			packets[i][12] = (byte) ack;
			int fin = 0;
			packets[i][13] = (byte) fin;

			packets[i][14] = (byte) (windowSize >> 8);
			packets[i][15] = (byte) (windowSize);

			checkSum = checkSum(packets[i]);

			packets[i][16] = (byte) (checkSum >> 8);
			packets[i][17] = (byte) (checkSum);

			short dataLength = 556;
			packets[i][18] = (byte) (dataLength >> 8);
			packets[i][19] = (byte) (dataLength); //urgent data is the length of the data sent
			j = 20; int k = 0;
			while(j<packets[i].length && ((sequenceNum + k) < allFileData.length) ) {
				packets[i][j] = allFileData[sequenceNum + k];
				j++; k++;
			}
		//String test = new String(packets[i]);
		//System.out.println(test);
		}
		//make last one special fin numSegments

		int fin = 1;
		packets[numSegments-1][13] = (byte) fin;
		short dataLength = (short)(allFileData.length - sequenceNum);
		packets[numSegments-1][18] = (byte) (dataLength >> 8);
		packets[numSegments-1][19] = (byte) (dataLength); //urgent data is the length of the data sent
		System.out.println(dataLength);
		return packets;
	}

	public static int checkSum(byte[] packet){
		int i = 0;
		int checkSum = 0;
		while(i < 16){
			checkSum = packet[i] ^ (byte)checkSum;
			i++;
		}
		return checkSum;
	}

	public static int shiftWindowCheck(){
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

	public static void shiftWindow(int shift){
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

	public static byte[] readFile(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

	public static void main(String[] args){
	}

	public void run(){
		while(true){
			event = getEvent();
			switch(event){
				case 1 : //send some data
					try{
    					//byte[] sendData = new byte[1024];
    					//String sentence = "blah blah";
    					//sendData = sentence.getBytes();
						nextSeqNum = base;
						int i = 0;
						if(base < packets.length){
							while(nextSeqNum < base + windowSize){
								System.out.println("seqnum = " + nextSeqNum);
								if(windowAck[i] == false && nextSeqNum < packets.length){
    								byte[] packet = packets[nextSeqNum];
    								DatagramPacket sendPacket = new DatagramPacket(packet, packet.length, iPAddress, portNumDest);
      								sendSocket.send(sendPacket);
								}
								i++;
      							nextSeqNum++;
							}
						} else {
							System.out.println("Done sending file");
						}
					} catch(Exception e){
						e.printStackTrace();
					}
					aCKRecv = true; //maybe i don't need this?!?!
					break;
				case 2 : //timeout
					break;
				case 3 : // Listen for ack
					try{
						int basePrev = base;
						while(basePrev - base == 0){//base has not changed
							byte[] receiveACKData = new byte[4];
							DatagramPacket receiveACKPacket = new DatagramPacket(receiveACKData, receiveACKData.length);
  							ackSocket.receive(receiveACKPacket);
  							int ack = (int)( ((receiveACKData[0]&0xFF)<<24) | ((receiveACKData[1]&0xFF) << 16) | ((receiveACKData[2]&0xFF)<<8) | (receiveACKData[3]&0xFF) );
							System.out.println("ack = " + ack);
							System.out.println("base = " + base);
							if(ack >= base){
								windowAck[ack - base] = true;
							}
							//System.out.println("ack=" + ack);
							int shiftWindowCount = shiftWindowCheck();
							shiftWindow(shiftWindowCount);
							base += shiftWindowCount;
							//System.out.println(base);
						}
					} catch(Exception e){
						e.printStackTrace();
					}
					dataRecv = true;//looks like i dont need this??
					break;
				case 0 :
					break;
				}
		}
	}

}