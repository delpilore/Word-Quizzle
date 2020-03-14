package source;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WQClient {
	
	public static void main(String[] args) {
		
		Remote remoteObject;
		RegisterInterface serverObject;
		
		try {
			Registry r = LocateRegistry.getRegistry(30000);
			remoteObject = r.lookup("REGISTER-SERVER");
			serverObject = (RegisterInterface) remoteObject;
			
			if (serverObject.registra_utente("cicchio", "adbsds") == 1)
				System.out.println("Registrato!");
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}