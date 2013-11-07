import java.io.*;
import java.net.*;
import java.lang.*;


public class TCPSender /*implements Runnable*/{
	public boolean dataRecv;
	public boolean timeOut;
	public boolean aCKRecv;
	public int event;
	public DatagramSocket sendSocket;
    public InetAddress iPAddress;
    public int portNum;
    //file name also

	public TCPSender(){
		this.dataRecv = false;
		this.event = 0;
		try{
			this.sendSocket = new DatagramSocket();
			this.portNum = 8888;
			this.iPAddress = InetAddress.getByName("localhost");
		} catch (Exception e){
			System.err.println(e);
		}
	}

	public TCPSender(String iPAddress, int portNum){ //probably will take in a file name also
		this.dataRecv = false;
		this.event = 0;
		try{
		this.iPAddress = InetAddress.getByName(iPAddress);
		this.portNum = portNum;
		this.sendSocket = new DatagramSocket();
		} catch (Exception e){
			System.err.println(e);
		}
	}

	public void init(){
		while(true){
			event = getEvent();
			switch(event){
				case 1 :
					try{
						System.out.println("in try block in TCPSender");
    					byte[] sendData = new byte[1024];
    					String sentence = "blah blah";
    					sendData = sentence.getBytes();
    					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, this.iPAddress, portNum);
      					this.sendSocket.send(sendPacket);
					} catch(Exception e){
						System.err.println(e);
					}
      				event = 0;
					break;
				case 2 :
      				event = 0;
					break;
				case 3 :
      				event = 0;
					break;
				case 0 :
					event = 0;
					break;
				}
		}
	}

	public void dataRecv(){
		this.dataRecv = true;
	}

	public int getEvent(){
		if(this.dataRecv){
			this.dataRecv = false;
			return 1;
		}
		if(this.timeOut){
			this.timeOut = false;
			return 2;
		}
		if(this.aCKRecv){
			this.aCKRecv = false;
			return 3;
		}
		return 0;
	}

	public static void main(String[] args){
		//new Thread(new TCPSender()).start();
	}

	//public void run(){
	//
	//}

}