package source;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterInterface extends Remote {
	
	boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, NullPointerException, UsernameTooShortException, WeakPasswordException ;
	
	@SuppressWarnings("serial")
	class UserAlreadyRegisteredException extends Exception {

		public UserAlreadyRegisteredException() {
            super();
        }
        
        public UserAlreadyRegisteredException(String s) {
            super(s);
        }
    }
	
	@SuppressWarnings("serial")
	class UsernameTooShortException extends Exception {

		public UsernameTooShortException() {
            super();
        }
        
        public UsernameTooShortException(String s) {
            super(s);
        }
    }
	
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
