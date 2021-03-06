package udp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import sun.audio.*;

/**
 * The Client class.
 * @see udp.Client
 */
public class ClientImpl implements Client{

	/** The host name. */
	private String hostName;
	
	/** The client. */
	private Socket client;
	
	/** The port. */
	private int port;
	
	/** The unique id. */
	private Integer uniqueId;
	
	/** The process. */
	private String process;
	
	/** The UDP socket. */
	private DatagramSocket UDPSocket;
	
	/** The file stream. */
	private FileInputStream fileStream;
	
	/** The file to send. */
	private File fileToSend;
	
	/** The to server. */
	private DataOutputStream toServer;
	
	/** The from server. */
	private BufferedReader fromServer;
	
	
	/**
	 * Instantiates a new client with the supplied hostname and port number.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public ClientImpl(String host, int port){
		this.hostName = host;
		this.port = port;
	}
	
	/**
	 * The run method which intiates the client/server commnication.
	 */
	public void run(){
		connectToServerViaTCP();
		requestUniqueId(client);
		isFirstToConnect();
		receiveInstructionForUDP();
			if(process.equals("sender")){
				sendViaUDP();
			} else {
				receiveViaUDP();
			}
	}
	
	/**
	 * @see udp.Client#connectToServerViaTCP()
	 */
	@Override
	public void connectToServerViaTCP() {
		try{
			System.out.println("TRYING TO CONNECT");
			client = new Socket(hostName, port);
			System.out.println("CONNECTED TO HOST: " + hostName + " AT PORT: " + port);
		} catch (UnknownHostException ex){
			ex.printStackTrace();
		} catch (IOException ex){
			ex.printStackTrace();
		}
		
	}

	/**
	 * @see udp.Client#requestUniqueId(java.net.Socket)
	 */
	@Override
	public void requestUniqueId(Socket client) {
		try {
			toServer = new DataOutputStream(client.getOutputStream());
			fromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
			System.out.println("REQUESTING UNIQUE ID");
			String idRequest = "send id";
			toServer.writeBytes(idRequest + '\n');
			toServer.flush();
			String receivedId = fromServer.readLine();
			uniqueId = Integer.parseInt(receivedId);
			System.out.println("UNIQUE ID IS: " + uniqueId);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @see udp.Client#isFirstToConnect()
	 */
	@Override
	public void isFirstToConnect() {
		try {
			String firstRequest = "first to connect?";
			toServer.writeBytes(firstRequest + '\n');
			toServer.flush();
			String receivedText = fromServer.readLine();
				if(receivedText.equals("Yes")){
					process = "sender";
				} else {
					process = "receiver";	
				}	
		System.out.println("CLIENT " + uniqueId + " IS A: " + process);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * @see udp.Client#receiveInstructionForUDP()
	 */
	@Override
	public void receiveInstructionForUDP() {
		try {
			String instruction = fromServer.readLine();
			System.out.println(instruction);
				if (instruction.equals("CONNECT OVER UDP.")){
					System.out.println("INSTRUCTION RECEIVED: " + instruction);
					UDPSocket = new DatagramSocket();
				}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
	}

	/**
	 * @see udp.Client#sendViaUDP()
	 */
	@Override
	public void sendViaUDP() {
		try {
			byte[] dataToSend;
			InetAddress IPAddress = InetAddress.getByName("localHost");
			fileToSend = new File("AudioFile1.wav");
		    int size = (int) fileToSend.length();
		    dataToSend = new byte[size];
		    fileStream = new FileInputStream(fileToSend);
		    System.out.println("SENDING: " + size + " bytes");
		    fileStream.read(dataToSend);
		    DatagramPacket packetToSend = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, 2000);
		    UDPSocket.send(packetToSend);
		    System.out.println("SENDING COMPLETE");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @see udp.Client#receiveViaUDP()
	 */
	@Override
	public void receiveViaUDP() {
		try {
			String toSend = "READY FOR AUDIO";
    		byte[] dataToSend = toSend.getBytes();
			InetAddress IPAddress = InetAddress.getByName("localHost");
			DatagramPacket sendPacket = new DatagramPacket(dataToSend, dataToSend.length, IPAddress, 2000);
			UDPSocket.send(sendPacket);
			byte[] dataReceived = new byte [100000];
			DatagramPacket receivePacket = new DatagramPacket(dataReceived, dataReceived.length);
			UDPSocket.receive(receivePacket);
            System.out.println("RECEIVED: " + receivePacket.getLength() + " bytes");
            playAudio(receivePacket.getData());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * @see udp.Client#playAudio(byte[])
	 */
	public void playAudio(byte [] audioBytes){
		System.out.println("PLAYING AUDIO");
		AudioData audioData = new AudioData(audioBytes);
		AudioDataStream audioStream = new AudioDataStream(audioData);
		AudioPlayer.player.start(audioStream);
	}

}
