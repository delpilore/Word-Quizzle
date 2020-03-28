package source;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
 * LISTENER (oggetto istanziato una sola volta in "WQServer" e dato come task ad un Thread)
 * 
 * Questa classe che implementa l'interfaccia Runnable, vuole rappresentare un classico task da "accettatore di connessioni" TCP.
 * Nell'economia di Word Quizzle sarà un solo Thread a prendere in carico questo task, avendo a disposizione l'accesso ad una 
 * LinkedBlockingQueue condivisa dove inserire le socket relative ai client accettati, le cui richieste saranno soddisfatte 
 * successivamente da una pool di thread (istanziata in "WQServer", con "RequestHandler" come task da eseguire per tutti i worker).
 */

public class Listener implements Runnable {

	// Rispettivamente: porta da cui il server ascolterà le richieste di connessione, socket d'accettazione, LinkedBlockingQueue
	// condivisa con gli altri thread del server e passata come argomento al costruttore in fase di istanziazione.
	private int myTCPPort = 16000;
	private ServerSocket server = null;																
	private LinkedBlockingQueue<Socket> queue;
	
	// Costruttore a cui viene passato il riferimento alla LinkedBlockingQueue condivisa
	public Listener (LinkedBlockingQueue<Socket> _queue) {
		queue = _queue;
	}
	
	// Task eseguito dal thread
	public void run() {
		try {
			server = new ServerSocket(myTCPPort);
			System.out.println("Server in attesa di connessioni sulla porta: " + myTCPPort);
	
			while(true) {
				Socket client = server.accept();
				System.out.println("Accettato client: " + client);
				
				// Metto nella LinkedBlockingQueue condivisa la socket del client accettato, le cui richieste
				// saranno gestite da una pool di thread (come detto sopra)
				synchronized(queue) {
					queue.add(client);
					queue.notify();
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}