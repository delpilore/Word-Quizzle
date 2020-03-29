package wordquizzle.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

import wordquizzle.GeneralUtilities;

public class ChallengeListener implements Runnable {
	
	private DatagramSocket UDPSocket;
	private DatagramPacket UDPPackReceive = null;
	private int myUDPPort;
	private int serverUDPPort;
	private Boolean challenged = false;
	private Boolean inChallenge = false;

	public ChallengeListener(int _port) {
		myUDPPort = _port;
	}
	
	public void run() {
		try {
			UDPSocket = new DatagramSocket(myUDPPort);
	        while (true) { 
	        	byte[] receive = new byte[1000]; 
	            UDPPackReceive = new DatagramPacket(receive, receive.length); 
	            UDPSocket.receive(UDPPackReceive);
	            
	            if(inChallenge==false) {
		            System.out.println("\n" + GeneralUtilities.data(receive).toString() + " ti ha sfidato! Accetti?");
		            System.out.println("\ty: accetta");
		            System.out.println("\tn: rifiuta");
	
		            serverUDPPort = UDPPackReceive.getPort();
		            setChallenged(true);
		            Timer timer = new Timer();
		            TimerTask task = new OutOfTime(this);
	
		            timer.schedule( task, 5000 );
	            }
	            else {
	            	if(!GeneralUtilities.data(receive).toString().equals("FINE"))
	            		System.out.println("\nEcco la parola: " + GeneralUtilities.data(receive).toString());
	            	else {
	            		System.out.println("\nPARITA FINITA!");
	            		setInChallenge(false);
	            		
	    	        	byte[] result = new byte[10000]; 
	    	            DatagramPacket UDPPackResult = new DatagramPacket(result, result.length); 
	    	            UDPSocket.receive(UDPPackResult);
	    	            
	    	            System.out.println(GeneralUtilities.data(result).toString());
	    	            System.out.println("Puoi tornare alle tue classiche operazioni!");
	            	}
	            }
	        }
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public int getServerUDPPort() {
		return serverUDPPort;
	}

	public DatagramSocket getDatagramSocket() {
		return UDPSocket;
	}

	public Boolean isChallenged() {
		return challenged;
	}

	public void setChallenged(Boolean challenged) {
		this.challenged = challenged;
	}

	public void setInChallenge(Boolean inChallenge) {
		this.inChallenge = inChallenge;
	}
	
	public Boolean isInChallenge() {
		return inChallenge;
	}
}