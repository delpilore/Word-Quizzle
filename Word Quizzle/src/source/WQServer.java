package source;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
 * WQSERVER 
 * 
 * WQServer altro non è che il core del Server. 
 * Si occupa di:
 *  - Istanziare un singolo oggetto "Structures", che sarà poi condiviso tra tutti i Thread del Server.
 * 	- Esportare l'oggetto che realizzerà l'operazione di registrazione di un client (in RMI). 
 * 	- Avviare tutti i Thread necessari al funzionamento del Server (Listener e ThreadPool).
 */
public class WQServer {
	
	public static void main(String[] args) throws InterruptedException {
		
		// Oggetto Structures istanziato una sola volta e condiviso tra tutti i Thread del server (vedere "Structures")
		Structures WordQuizzleSupport = new Structures();
		
		int NWORKERS = 2;
		int myRMIPort = 15000;
		RegisterImpl register = null;
		
		// Esportazione dell'oggetto che realizza l'operazione di registrazione
		try {
			register = new RegisterImpl(WordQuizzleSupport);
			RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(register, 0);

			LocateRegistry.createRegistry(myRMIPort);
			Registry r = LocateRegistry.getRegistry(myRMIPort);

			r.rebind("REGISTER-SERVER", stub);	
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		
		// Istanzio la ThreadPool
		ThreadPoolExecutor poolWorker = (ThreadPoolExecutor) Executors.newFixedThreadPool(NWORKERS);
		
		// Istanzio un task Listener passandogli la queue dove inserirà le socket dei client accettati (vedere "Listener")
		Listener listener = new Listener(WordQuizzleSupport.getRequestsQueue());
		Thread Welcome = new Thread(listener);
		Welcome.start();
		
		// Faccio partire i Thread Worker che eseguiranno tutti il task "RequestHandler"
		for (int i=0; i<NWORKERS; i++)
			poolWorker.execute(new RequestHandler(WordQuizzleSupport));
		
	}	
}