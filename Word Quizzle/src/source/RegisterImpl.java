package source;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;


public class RegisterImpl extends RemoteServer implements RegisterInterface {

	private static final long serialVersionUID = 1L;

	private Structures WordQuizzleUsers;
	
	public RegisterImpl(Structures _support) throws RemoteException {
		WordQuizzleUsers = _support;
	}
	
	public boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, NullPointerException {
		
		if (WordQuizzleUsers.containsUser(nickUtente))
			throw new UserAlreadyRegisteredException();
		
		if (nickUtente==null || password==null)
			throw new NullPointerException();
		
		WordQuizzleUsers.addUser(nickUtente, new User(nickUtente, password));
		WordQuizzleUsers.writeJson();
		
		System.out.println("Utente " + nickUtente + " registrato con successo!");
	
		return true;
	}
}	