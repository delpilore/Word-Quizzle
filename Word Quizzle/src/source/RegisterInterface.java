package source;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterInterface extends Remote {
	int registra_utente (String nickUtente, String password) throws RemoteException;
}
