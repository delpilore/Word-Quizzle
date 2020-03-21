package source;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class WQServer {
	
	public static void main(String[] args) {
		
		Structures support = new Structures();
		
		try {
			RegisterImpl register = new RegisterImpl(support);
			RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(register, 0);

			LocateRegistry.createRegistry(15000);
			Registry r = LocateRegistry.getRegistry(15000);

			r.rebind("REGISTER-SERVER", stub);

			System.out.println("Server ready");
			
			/* Stop dell'input JSON */
			Scanner input = new Scanner(System.in);
			String command;
			command = input.next();
			if(command.equals("stop"))
				register.stopJSON();
			input.close();
			
		}
		catch(RemoteException e){
			e.printStackTrace();
		}
	}
}