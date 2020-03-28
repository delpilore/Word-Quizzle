package wordquizzle.server.structures;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import wordquizzle.server.User;

public class RegisteredUsers {

	private Hashtable<String, User> users; 
	
	private File json_file;
	private ObjectMapper objectMapper;
	
	public RegisteredUsers() {
		users = new Hashtable<String, User>();
		
		json_file = new File("RegisteredUserState.json");	
		objectMapper = new ObjectMapper();
	}
	
	public void registerUser(String _username, User _user) {
		users.put(_username, _user);
	}
	
	public boolean isRegistered(String _username) {
		return users.containsKey(_username);
	}
	
	public User getUser(String _user) {
		return users.get(_user);
	}
	
	public void fetchPreviousState(){

		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		if(json_file.isFile()) {
			System.out.println("Il file JSON esiste già, recupero tutti gli utenti dal file\n");

			try {
				BufferedReader br = new BufferedReader(new FileReader(json_file));
				
				if (br.readLine() != null) {
					// Recupero WordQuizzleUsers (stato precedente del server) attraverso il file json_file e
					// stampo tutto il contenuto di essa per vedere se è consistente
					users = objectMapper.readValue(json_file, new TypeReference<Hashtable<String,User>>(){});
					
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
		
	public void writeJson() {
		try {
			objectMapper.writeValue(json_file, users);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}