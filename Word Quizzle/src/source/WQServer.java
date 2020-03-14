package source;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class WQServer {
	
	public static void main(String[] args) {
		
		try {
			RegisterImpl register = new RegisterImpl();
			RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(register, 0);

			LocateRegistry.createRegistry(30000);
			Registry r = LocateRegistry.getRegistry(30000);

			r.rebind("REGISTER-SERVER", stub);

			System.out.println("Server ready");
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
	}
}