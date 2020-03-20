package source;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Hashtable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RegisterImpl extends RemoteServer implements RegisterInterface {

	private static final long serialVersionUID = 1L;
	Hashtable<String, String> registered_users;
	ObjectMapper objectMapper = new ObjectMapper();
	File json_file;
	
	public RegisterImpl() throws RemoteException {
		json_file = new File("registered_users.json");
		if(!json_file.isFile()) {
			registered_users = new Hashtable <String, String>();
		}
		else {
			System.out.println("Il file JSON esiste già, recupero i valori dal file");
			try {
				registered_users = objectMapper.readValue(json_file, new TypeReference<Hashtable<String, String>>(){});
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Gli utenti registrati sono: " + registered_users); 
		}
		
	}
	
	
	public boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, NullPointerException {
		
		if (registered_users.containsKey(nickUtente))
			throw new UserAlreadyRegisteredException();
		
		if (nickUtente==null || password==null)
			throw new NullPointerException();
		
		registered_users.put(nickUtente, password);
		System.out.println("Utente " + nickUtente + " registrato con successo!");
		try {
			objectMapper.writeValue(json_file, registered_users);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
}
