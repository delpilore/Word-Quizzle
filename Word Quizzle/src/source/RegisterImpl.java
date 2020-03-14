package source;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Hashtable;

public class RegisterImpl extends RemoteServer implements RegisterInterface {

	private static final long serialVersionUID = 1L;
	Hashtable<String, String> registered_users;
	
	public RegisterImpl() throws RemoteException {
		registered_users = new Hashtable <String,String>();
	}
	public int registra_utente (String nickUtente, String password) throws RemoteException {
		registered_users.put(nickUtente, password);
		System.out.println("Utente " + nickUtente + " registrato con successo!");
		return 1;
	}
}
