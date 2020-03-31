package wordquizzle.server;

import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import wordquizzle.RegisterInterface;
import wordquizzle.server.structures.ChallengeableUsers;
import wordquizzle.server.structures.CurrentMatches;
import wordquizzle.server.structures.RegisteredUsers;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
 * WQSERVER 
 * 
 * L'applicazione Word Quizzle è implementata secondo una architettura Client-Server.
 * Il core di quest'ultimo è proprio il seguente programma WQServer.
 * Si occupa di:
 *  - Istanziare tutte le strutture necessarie al funzionamento del server (vedere i file contenuti in wordquizzle.server.structures)
 * 	- Esportare l'oggetto che realizzerà l'operazione di registrazione di un client (in RMI). 
 * 	- Avviare tutti i Thread necessari al funzionamento del Server (Listener e ThreadPool).
 */

public class WQServer {
	
	public static void main(String[] args) throws InterruptedException {
		
		// Istanziazione di tutte le strutture necessarie al funzionamento del server (vedere i file relativi contenuti in wordquizzle.server.structures)
		RegisteredUsers registeredUsers = new RegisteredUsers(); // Utenti registrati
		ChallengeableUsers challengeableUsers = new ChallengeableUsers(); // Utenti sfidabili (online e non in partita o con richieste pendenti)
		CurrentMatches currentMatches = new CurrentMatches(); // Partite in corso 
		LinkedBlockingQueue<Socket> activeRequests = new LinkedBlockingQueue<Socket>(); // Coda di Socket condivisa tra Listener e Workers 
		
		// Riporto allo stato della precedente esecuzione del server (persistenza) l'oggetto registeredUsers.
		// Recupero tutte le informazioni da un file Json (vedere "RegisteredUsers" per i dettagli implementativi)
		registeredUsers.fetchPreviousState();
		
		// Rispettivamente: numero di thread worker attivati, porta associata al servizio registrazione in RMI, oggetto
		// da esportare non ancora istanziato.
		int NWORKERS = 4;
		int myRMIPort = 15000;
		RegisterImpl register = null;
		
		// Esportazione dell'oggetto che realizza l'operazione di registrazione
		try {
			register = new RegisterImpl(registeredUsers);
			RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(register, 0);

			LocateRegistry.createRegistry(myRMIPort);
			Registry r = LocateRegistry.getRegistry(myRMIPort);

			r.rebind("REGISTER-SERVER", stub);	
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		
		// Istanzio la ThreadPool come una FixedThreadPool (numero di thread fissato)
		ThreadPoolExecutor poolWorker = (ThreadPoolExecutor) Executors.newFixedThreadPool(NWORKERS);
		
		// Faccio partire un task Listener passandogli la LinkedBlockingQueue istanziata precedentemente.
		// Il Listener la utilizzerà per accodare le socket dei client accettati (vedere "Listener")
		// che verranno poi gestiti dai Worker. 
		Listener listener = new Listener(activeRequests);
		Thread Welcome = new Thread(listener);
		Welcome.start();
		
		// Faccio partire i Thread Worker che eseguiranno il task "RequestHandler" passando loro, inoltre, tutte le strutture istanziate inizialmente. 
		for (int i=0; i<NWORKERS; i++)
			poolWorker.execute(new RequestHandler(registeredUsers, challengeableUsers, currentMatches, activeRequests));		
	}	
}