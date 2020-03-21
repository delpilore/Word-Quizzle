package source;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

public class RegisterImpl extends RemoteServer implements RegisterInterface {

	private static final long serialVersionUID = 1L;

	ObjectMapper objectMapper = new ObjectMapper();
	File json_file;
	SequenceWriter seqWriter;
	Structures support;
	
	public RegisterImpl(Structures _support) throws RemoteException {
		
		support = _support;
		json_file = new File("registered_users.json");
		
		if(!json_file.isFile()) {
			try {
				seqWriter = objectMapper.writer().writeValuesAsArray(json_file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Il file JSON esiste già, recupero i valori dal file");
			try {
				List<User> users = Arrays.asList(objectMapper.readValue(json_file, User[].class));
				
				// Dopo aver recuperato tutti gli utenti registrati mettendoli in una lista, li riscrivo dentro la Hashtable contenuta nell'oggetto support
				// e pure nel file JSON.
				seqWriter = objectMapper.writer().writeValuesAsArray(json_file);
				for (User i : users) {
					seqWriter.write(i); // Scrivo nel file JSON
					support.reg_addUser(i.getUsername(),i); // Scrivo nella Hashstable di support
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("Gli utenti registrati sono: " + support.getRegistered()); // Stampo tutto il contenuto della Hashtable dei registrati per vedere se è consistente
																						  // con la vecchia esecuzione del server.
		}
		
	}
	
	public boolean registra_utente (String nickUtente, String password) throws RemoteException, UserAlreadyRegisteredException, NullPointerException {
		
		if (support.reg_containsUser(nickUtente))
			throw new UserAlreadyRegisteredException();
		
		if (nickUtente==null || password==null)
			throw new NullPointerException();
		
		support.reg_addUser(nickUtente, new User(nickUtente, password));
		
		System.out.println("Utente " + nickUtente + " registrato con successo!");
		try { 
			seqWriter.write(new User(nickUtente, password));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	void stopJSON() {
		try {
			seqWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
}