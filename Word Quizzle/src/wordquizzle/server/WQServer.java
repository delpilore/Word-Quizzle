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
 *  - Istanziare un singolo oggetto "Structures", che sarà poi condiviso tra tutti i Thread del Server.
 * 	- Esportare l'oggetto che realizzerà l'operazione di registrazione di un client (in RMI). 
 * 	- Avviare tutti i Thread necessari al funzionamento del Server (Listener e ThreadPool).
 */

public class WQServer {
	
	public static void main(String[] args) throws InterruptedException {
		
		RegisteredUsers registeredUsers = new RegisteredUsers();
		ChallengeableUsers challengeableUsers = new ChallengeableUsers();
		CurrentMatches currentMatches = new CurrentMatches();
		LinkedBlockingQueue<Socket> activeRequests = new LinkedBlockingQueue<Socket>();
		
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
		
		// Faccio partire un task Listener passandogli la BlockingQueue contenuta nell'oggetto Structures
		// istanziato precedentemente.
		// Il Listener la utilizzerà per accodare le socket dei client accettati (vedere "Listener")
		// che verranno poi gestiti dai Worker. (ricordiamo che l'unico oggetto Structures, è condiviso tra
		// tutti i thread del server)
		Listener listener = new Listener(activeRequests);
		Thread Welcome = new Thread(listener);
		Welcome.start();
		
		// Faccio partire i Thread Worker che eseguiranno il task "RequestHandler"
		for (int i=0; i<NWORKERS; i++)
			poolWorker.execute(new RequestHandler(registeredUsers, challengeableUsers, currentMatches, activeRequests));		
	}	
}