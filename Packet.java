import java.io.*;

public class Packet implements Comparable{
	public byte[] bytes;
	public byte[] data;
	public int sequenceNum;
	public int ackNum;
	public int ack;
	public int fin;
	public int dataLength;
	
	public Packet(byte[] bytes){
  		this.bytes = bytes;
		this.dataLength = getShort(bytes, 18);
		this.sequenceNum = getInt(bytes, 4);
		this.ackNum = getInt(bytes, 8);
		this.ack = (int)(bytes[12]&0xFF);
		this.fin = (int)(bytes[13]&0xFF);
		this.data = fetchData(bytes, dataLength); //get data from bytes
	}

	public static byte[] fetchData(byte[] bytes, int dataLength){
		byte[] data = new byte[dataLength];
  		int i = 0;
  		while(i<dataLength){
  			data[i] = bytes[i + 20];
  			i++;
  		}
  		return data;
	}

	public byte[] getBytes(){
		return this.bytes;
	}

	public byte[] getData(){
		return this.data;
	}

	public static byte[][] createPackets(short sourcePort, short destPort, int windowSize, String fileName){ //convert file into array of packets or byte[][]
		byte[] allFileData = new byte[1];
		
		String fileString = "";
		try{
			File file = new File(fileName);
			allFileData = readFile(file);
		} catch(Exception e){
			e.printStackTrace();
		}

		char[] charArray = fileString.toCharArray();
		int numSegments = (int) Math.ceil((double)allFileData.length / 556); //needs to round up
		int packetSize = 576;
		short checkSum;
		int sequenceNum = 0;
		int ackNum = 0;
		int ack = 1;
		int fin = 0;
		short dataLength = 556;

		byte[][] packets = new byte[numSegments][packetSize];

		for(int i = 0; i<numSegments; i++){
			int j = 0;
			setShort(packets[i], 0, sourcePort);
			setShort(packets[i], 2, destPort);

			sequenceNum = i * 556;
			ackNum = i;

			setInt(packets[i], 4, sequenceNum);
			setInt(packets[i], 8, ackNum);

			packets[i][12] = (byte) ack;
			packets[i][13] = (byte) fin;

			setShort(packets[i], 14, (short)windowSize);
			setShort(packets[i], 18, dataLength);

			j = 20; int k = 0;
			while(j<packets[i].length && ((sequenceNum + k) < allFileData.length) ) {
				packets[i][j] = allFileData[sequenceNum + k];
				j++; k++;
			}

			checkSum = checkSum(packets[i]);
			setShort(packets[i], 16, checkSum);
		}
		//make last one special fin numSegments
		int finIndex = numSegments-1;
		fin = 1;
		packets[finIndex][13] = (byte) fin;
		dataLength = (short)(allFileData.length - sequenceNum);
		setShort(packets[finIndex], 18, dataLength);

		checkSum = checkSum(packets[finIndex]);
		setShort(packets[finIndex], 16, checkSum);

		return packets;
	}

	public static void setInt(byte[] bytes, int i, int num){
		bytes[i] 	 = (byte) (num >> 24);
		bytes[i + 1] = (byte) (num >> 16);
		bytes[i + 2] = (byte) (num >> 8);
		bytes[i + 3] = (byte) num;
	}

	public static void setShort(byte[] bytes, int i, short num){
		bytes[i]	 = (byte) (num >> 8);
		bytes[i + 1] = (byte) num;
	}

	public static int getInt(byte[] bytes, int i){
 		return (int)(((bytes[i]&0xFF)<<24) | ((bytes[i+1]&0xFF) << 16) | ((bytes[i+2]&0xFF)<<8) | (bytes[i+3]&0xFF));
	}

	public static short getShort(byte[] bytes, int i){
		return (short)(((bytes[i]&0xFF)<<8) | (bytes[i+1]&0xFF));
	}

	//Credit is due where credit is deserved
	//readFile came from here:
	//http://stackoverflow.com/questions/6058003/elegant-way-to-read-file-into-byte-array-in-java
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

    public static short checkSum(byte[] packet){
		int i = 0;
		int checkSum = 0;
		while(i < 16){
			checkSum = packet[i] ^ (byte)checkSum;
			i++;
		}
		i = 18;
		while(i < 576){
			checkSum = packet[i] ^ (byte)checkSum;
			i++;
		}
		return (short)checkSum;
	}

	public int compareTo(Object other){
		Packet that;
		if(other instanceof Packet) {
			that = (Packet) other;
		} else {
			System.err.println("did not compare Packet to Packet");
			return 1;
		}
		return this.ackNum < that.ackNum ? -1 : this.ackNum > that.ackNum ? 1 : 0;
	}
}