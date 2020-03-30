package wordquizzle;

import java.rmi.Remote;
import java.rmi.RemoteException;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* REGISTERINTERFACE
* 
* Interfaccia dell'oggetto remoto che si occuperà di realizzare l'operazione di registrazione.
*/

public interface RegisterInterface extends Remote {
	
	// registra_utente(String nickUtente, String password)
	//
	// Metodo chiamato in RMI da parte del client, che aggiunge un nuovo oggetto User (vedere "User") 
	// alla tabella degli utenti registrati a Word Quizzle (vedere "RegisteredUsers" nel package wordquizzle.server.structures)
	// Realizza quindi una registrazione dell'utente.
	boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, 
																		NullPointerException, UsernameTooShortException, 
																		WeakPasswordException, UsernameTooLongException;
	
	// UserAlreadyRegisteredException 
	//
	// Eccezione checked non presente in Java.
	// Viene sollevata quando un client prova registrarsi al servizio utilizzando un username di un utente
	// già registrato.
	@SuppressWarnings("serial")
	class UserAlreadyRegisteredException extends Exception {

		public UserAlreadyRegisteredException() {
            super();
        }
        
        public UserAlreadyRegisteredException(String s) {
            super(s);
        }
    }
	
	// UsernameTooShortException
	//
	// Eccezione checked non presente in Java.
	// Viene sollevata quando un client prova registrarsi al servizio utilizzando un username 
	// più corto di 3 caratteri.
	@SuppressWarnings("serial")
	class UsernameTooShortException extends Exception {

		public UsernameTooShortException() {
            super();
        }
        
        public UsernameTooShortException(String s) {
            super(s);
        }
    }
	
	// UsernameTooLongException
	//
	// Eccezione checked non presente in Java.
	// Viene sollevata quando un client prova registrarsi al servizio utilizzando un username 
	// più lungo di 12 caratteri.
	@SuppressWarnings("serial")
	class UsernameTooLongException extends Exception {

		public UsernameTooLongException() {
            super();
        }
        
        public UsernameTooLongException(String s) {
            super(s);
        }
    }
	
	// WeakPasswordException
	//
	// Eccezione checked non presente in Java.
	// Viene sollevata quando un client prova registrarsi al servizio utilizzando un password 
	// più corta di 4 caratteri
	@SuppressWarnings("serial")
	class WeakPasswordException extends Exception {

		public WeakPasswordException() {
            super();
        }
        
        public WeakPasswordException(String s) {
            super(s);
        }
    }
}