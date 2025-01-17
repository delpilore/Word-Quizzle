package wordquizzle.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
 * LISTENER 
 * 
 * Questa classe che implementa l'interfaccia Runnable, vuole rappresentare un classico task da "accettatore di connessioni" TCP.
 * Nell'economia di Word Quizzle sar� un solo Thread a prendere in carico questo task, avendo a disposizione l'accesso ad una 
 * LinkedBlockingQueue condivisa dove inserire le socket relative ai client accettati, le cui richieste saranno soddisfatte 
 * successivamente da una pool di thread (istanziata in "WQServer", con "RequestHandler" come task da eseguire per tutti i worker).
 */

public class Listener implements Runnable {

	// Rispettivamente: porta da cui il server ascolter� le richieste di connessione, socket d'accettazione, LinkedBlockingQueue
	// condivisa con gli altri thread del server e passata come argomento al costruttore in fase di istanziazione.
	private int myTCPPort = 16000;
	private ServerSocket server = null;																
	private LinkedBlockingQueue<Socket> activeRequests;
	
	// Costruttore a cui viene passato il riferimento alla LinkedBlockingQueue condivisa
	public Listener (LinkedBlockingQueue<Socket> _activeRequests) {
		activeRequests = _activeRequests;
	}
	
	// Task eseguito dal thread
	public void run() {
		try {
			server = new ServerSocket(myTCPPort);
			System.out.println("Server in attesa di connessioni sulla porta: " + myTCPPort);
	
			while(true) {
				Socket client = server.accept();
				System.out.println("Accettato client: " + client);
				
				// Per ogni socket accettata (relativa ad un client) setto il timeout di essa a 100ms.
				// Lo faccio in modo che i worker non si blocchino sulle read in attesa di richieste dopo aver
				// completato la prima richiesta di login.
				client.setSoTimeout(100);
				activeRequests.offer(client);
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}