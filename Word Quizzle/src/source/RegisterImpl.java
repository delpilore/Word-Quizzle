package source;

import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* REGISTERIMPL
* 
* Oggetto remoto che si occuperà di realizzare l'operazione di registrazione.
*/

public class RegisterImpl extends RemoteServer implements RegisterInterface {

	private static final long serialVersionUID = -5222461577976164582L;
	
	// Da costruttore gli viene passato l'unico oggetto Structures del server, contenente, tra le altre,
	// la HashTable che mappa gli utenti registrati al servizio.
	private Structures WordQuizzleUsers;
	
	public RegisterImpl(Structures _support) throws RemoteException {
		WordQuizzleUsers = _support;
	}
	
	// registra_utente(String nickUtente, String password)
	//
	// Metodo chiamato in RMI da parte del client, che aggiunge un nuovo oggetto User (vedere "User") 
	// alla Hashtable principale del server, quella che mappa gli utenti registrati al servizio
	// e i loro dati. (vedere "Structures") 
	// Realizza quindi una registrazione dell'utente.
	public boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, NullPointerException, WeakPasswordException, UsernameTooShortException {
		
		// ECCEZIONI //
		
		if (nickUtente==null || password==null)
			throw new NullPointerException();
		
		if (password.length()<4) 
			throw new WeakPasswordException();
		
		if (nickUtente.length()<3)
			throw new UsernameTooShortException();
		
		if (WordQuizzleUsers.containsUser(nickUtente))
			throw new UserAlreadyRegisteredException();
		
		// FINE ECCEZIONI //

		// Aggiungo una nuova coppia <nickUtente, new User> alla HashTable dei registrati.
		// Successivamente, chiamando writeJson() (vedere "Structures") scrivo/aggiorno il file 
		// json relativo allo stato attuale degli utenti registrati.
		WordQuizzleUsers.addUser(nickUtente, new User(nickUtente, password));
		WordQuizzleUsers.writeJson();
		
		System.out.println("Utente " + nickUtente + " registrato con successo!");
	
		return true;
	}
}	