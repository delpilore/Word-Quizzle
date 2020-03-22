package source;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WQServer {
	
	public static void main(String[] args) throws InterruptedException {
		
		Structures WordQuizzleUsers = new Structures();
		
		int NWORKERS = 2;
		int myTCPPort = 16000;
		int myRMIPort = 15000;
		RegisterImpl register = null;
		
		try {
			register = new RegisterImpl(WordQuizzleUsers);
			RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(register, 0);

			LocateRegistry.createRegistry(myRMIPort);
			Registry r = LocateRegistry.getRegistry(myRMIPort);

			r.rebind("REGISTER-SERVER", stub);	
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
		
		ServerSocket server = null;
		ExecutorService threadPool = Executors.newFixedThreadPool(NWORKERS);
		
		try {
			server = new ServerSocket(myTCPPort);
			System.out.println("Web server waiting for request on port " + myTCPPort);

			while(true) {
				Socket client = server.accept();
				threadPool.execute(new RequestHandler(client, WordQuizzleUsers));
			}	
		}
		catch (IOException e) {
			threadPool.shutdown();
		}
	}
}