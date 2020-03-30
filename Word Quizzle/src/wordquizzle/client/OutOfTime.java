package wordquizzle.client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.TimerTask;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* OUTOFTIME
* 
* Task eseguito dopo 5 secondi dall'arrivo di una richiesta di sfida.
*/

public class OutOfTime extends TimerTask {
	
	ChallengeListener Listener;
	
	public OutOfTime(ChallengeListener _listener) {
		Listener = _listener;
	}
	
    public void run() {
    	// Questa routine avviene solo se l'utente risulta sempre "sfidato", ovvero
    	// se sono passati 5 secondi e lui non ha ancora accettato
    	if(Listener.isChallenged()) {
	        System.out.println( "Finito il tempo" );
			Listener.setChallenged(false);
	        
	    	try {
	        	InetAddress ip = InetAddress.getLocalHost(); 
	        
	        	// Mando come risposta al server "t", che interpreterà come messaggio di "outoftime", ovvero il client
	        	// non ha fatto in tempo a rispondere
	        	String command = new String("t");
	    		byte[] send = command.getBytes();
				DatagramPacket DpSend = new DatagramPacket(send, send.length, ip, Listener.getServerUDPPort()); 
				
				Listener.getDatagramSocket().send(DpSend);
	    	}
	    	catch(Exception e) {
	    		e.printStackTrace();
	    	}
	    	
	    	Listener.stopTimer();
    	}
    }
}