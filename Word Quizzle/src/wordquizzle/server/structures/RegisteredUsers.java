package wordquizzle.server.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import wordquizzle.server.User;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* REGISTEREDUSERS
* 
* Questa classe rappresenta gli utenti registrati al servizio WordQuizzle.
* Contiene inoltre 2 metodi particolari: uno per recuperare il precedente stato della struttura da un file Json 
* e l'altro per salvare lo stato attuale della struttura sullo stesso file Json.
*/

public class RegisteredUsers {

	private ConcurrentHashMap<String, User> users; 
	
	private File json_file;
	private ObjectMapper objectMapper;
	
	// Costruttore
	public RegisteredUsers() {
		users = new ConcurrentHashMap<String, User>();
		
		// File Json su cui verrà salvato/recuperato lo stato della struttura degli utenti registrati
		json_file = new File("RegisteredUserState.json");	
		objectMapper = new ObjectMapper();
	}
	
	// registerUser(String _username, User _user)
	//
	// Aggiunge un utente identificato completamente dal suo oggetto User, con chiave il suo nickname
	public void registerUser(String _username, User _user) {
		users.put(_username, _user);
	}
	
	// isRegistered(String _username)
	//
	// Restituisce true se l'utente con nickname _username risulta registrato, false altrimenti
	public boolean isRegistered(String _username) {
		return users.containsKey(_username);
	}
	
	// getUser(String _user)
	//
	// Restituisce l'oggetto User relativo al nickname _user 
	public User getUser(String _user) {
		return users.get(_user);
	}
	
	// fetchPreviousState()
	//
	// Questo metodo si occupa di recuperare lo stato precedente della tabella dei registrati.
	// Rende l'intero server persistente perché tutte le informazioni sugli utenti (completamente contenute negli oggetti User)
	// vengono recuperate e reinserite nella tabella.
	public void fetchPreviousState(){
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		if(json_file.isFile()) {
			System.out.println("Il file JSON esiste già, recupero tutti gli utenti dal file\n");

			try {
				BufferedReader br = new BufferedReader(new FileReader(json_file));
				
				if (br.readLine() != null) {
					// Recupero tutta la tabella attraverso il file json_file 
					users = objectMapper.readValue(json_file, new TypeReference<ConcurrentHashMap<String,User>>(){});
					
					// Setto a tutti lo stato offline (è normale che non sia consistente con la precedente esecuzione)
					Set<String> keys = users.keySet();
			        for(String key: keys){
			        	users.get(key).setOnlineState(false);
			        }
				}
				else
					System.out.println("Il file JSON esiste già ma è vuoto\n");
				br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.out.println("Il file JSON non esiste, avvio pulito del server\n");
	}
		
	// writeJson()
	//
	// Scrive la tabella dei registrati sul file Json 
	// Viene chiamato ogni volta c'è un aggiornamento importante della tabella e quindi di un utente.
	public void writeJson() {
		try {
			objectMapper.writeValue(json_file, users);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}