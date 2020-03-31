package wordquizzle.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;

import wordquizzle.GeneralUtilities;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* CHALLENGELISTENER
* 
* Task preso in carico da un Thread lato client, per l'ascolto e la ricezione di datagrammi UDP su una specifica socket.
* Il programma client, WQClient, istanzia questo task subito, passandogli la prima porta disponibile rintracciata 
* grazie al metodo portScanner() (vedere "ClientUtilities").
* 
* Nel concreto, il thread che prenderà in carico questo task, si occuperà di ricevere tutti i messaggi inerenti
* all'operazione Sfida: richieste di sfida, parole da tradurre, risultato finale della partita.
*/

public class ChallengeListener implements Runnable {
	
	private DatagramSocket UDPSocket;
	private DatagramPacket UDPPackReceive = null;
	
	// myUDPPort è la porta su cui la socket ascolterà (passata per argomento nel modo descritto sopra),
	// serverUDPPort è la porta su cui è piazzata la socket UDP del server, utile quando ci sarà bisogno di rispondere ad una 
	// richiesta di sfida inoltrata da quest'ultimo
	private int myUDPPort;	
	private int serverUDPPort;
	
	// Il flag challenged è true quando l'utente (client) associato a questo listener ha una richiesta di sfida
	// pendente, ovvero è stato "sfidato", ed è false quando invece non ce l'ha.
	// Il flag inChallenge è true quando l'utente (client) associato a questo listener è in partita, e false
	// quando non lo è.
	private Boolean challenged = false;
	private Boolean inChallenge = false;
	
	// Timer che farà partire un task schedulato (vedere "OutOfTime") in caso l'utente (client) associato a questo
	// listener non risponda alla richiesta di sfida entro 5 secondi.
	private Timer timer;

	// Costruttore 
	public ChallengeListener(int _port) {
		myUDPPort = _port;
	}
	
	// Metodo run()
	public void run() {
		try {
			// Apro la socketUDP in ascolto sulla porta passata
			UDPSocket = new DatagramSocket(myUDPPort);

			// Controllo ad ogni ciclo che il thread non sia stato interrotto per una richiesta di chiusura del Client
	        while (!Thread.currentThread().isInterrupted()) { 
	        	
	        	// Setto il timeout della socket a 100msec in modo da non bloccarmi qui e poter controllare
	        	// l'eventuale richiesta d'interruzione del thread.
	        	UDPSocket.setSoTimeout(100);
	        	
	        	byte[] receive = new byte[1000]; 
	            UDPPackReceive = new DatagramPacket(receive, receive.length); 
	            
	            try {
	            	UDPSocket.receive(UDPPackReceive);
	            }
	            catch(SocketTimeoutException e) {
	            	continue; // In caso avvenga il timeout vado semplicemente al ciclo successivo
	            }
	            
	            // Converto l'array di byte contenuto nel datagramma UDP in una stringa grazie al metodo UDPToString 
	            // contenuto in GeneralUtilities (vedere file relativo)
	            String str = GeneralUtilities.UDPToString(UDPPackReceive);
	            
	            // Controllo se al Listener, il client associato risulta in partita.
	            // Ciò che il listener si aspetta di ricevere, è differente a seconda che il client associato sia in partita o meno. 
	            // Nel caso non ci sia, si aspetta semplicemente delle richieste di sfida, nel caso invece ci sia, si aspetta 
	            // le parole da tradurre ed infine il risultato della partita
	            if(inChallenge==false) {
		            System.out.println("\n" + str + " ti ha sfidato! Accetti?");
		            System.out.println("\ty: accetta");
		            System.out.println("\tn: rifiuta");
		            
		            // Mi salvo la porta del server in modo da poter mandare la risposta alla richiesta di sfida da WQClient
		            serverUDPPort = UDPPackReceive.getPort();
		            
		            // Setto il flag challenged a true, perché sono stato sfidato, ho una richiesta di sfida a cui posso rispondere
		            setChallenged(true);
		            
		            // Preparo il timer con il task OutOfTime che partirà a partire da 5 secondi da ora
		            timer = new Timer();
		            TimerTask task = new OutOfTime(this);
		            timer.schedule( task, 5000 );
	            }
	            else {     
	            	// Se invece il client associato risulta in partita, mi aspetto le parole da tradurre o il risultato finale
	            	if(!str.equals("FINE"))
	            		System.out.println("\nEcco la parola: " + str);
	            	else {
	            		System.out.println("\nPARITA FINITA!");
	            		setInChallenge(false);
	            		
	            		// Tolgo il timeout dalla socket chiamando setSoTimeout(0), perché in questo caso la read si deve bloccare
	            		// nell'attesa del risultato, che può dipendere anche da quanto ci metterà l'avversario a finire
	            		UDPSocket.setSoTimeout(0);
	    	        	byte[] result = new byte[10000]; 
	    	            DatagramPacket UDPPackResult = new DatagramPacket(result, result.length); 

	    	            UDPSocket.receive(UDPPackResult);

	    	            String str2 = GeneralUtilities.UDPToString(UDPPackResult);
	    	            
	    	            System.out.println(str2);
	    	            System.out.println("Puoi tornare alle tue classiche operazioni!");
        				System.out.print( "\n\tLo: Effettua il logout (poi dal menù principale potrai chiudere)\n"
	       		                 + "\tA: Aggiungi un amico\n"
	    		                 + "\tLa: Vedi la tua lista amici\n"
	    		                 + "\tS: Sfida un amico\n"
	    		                 + "\tP: Vedi il tuo punteggio\n"
	    		                 + "\tC: Vedi la classifica con i tuoi amici\n");
	            	}
	            }
	        }
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
	}

	// Metodi getters e setters
	
	public int getServerUDPPort() {
		return serverUDPPort;
	}

	public DatagramSocket getDatagramSocket() {
		return UDPSocket;
	}

	public Boolean isChallenged() {
		return challenged;
	}

	public void setChallenged(Boolean _challenged) {
		this.challenged = _challenged;
	}

	public void setInChallenge(Boolean inChallenge) {
		this.inChallenge = inChallenge;
	}
	
	public Boolean isInChallenge() {
		return inChallenge;
	}
	
	// Fine metodi getters e setters
	
	// stopTimer()
	//
	// Metodo che cancella il timer in modo da chiudere il thread relativo al task schedulato
	public void stopTimer() {
		timer.cancel();
	}
}