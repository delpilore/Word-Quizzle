package source;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterInterface extends Remote {
	
	boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, NullPointerException;
	
	@SuppressWarnings("serial")
	class UserAlreadyRegisteredException extends Exception {

		public UserAlreadyRegisteredException() {
            super();
        }
        
        public UserAlreadyRegisteredException(String s) {
            super(s);
        }
    }
}
