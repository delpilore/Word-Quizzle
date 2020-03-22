package source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Structures {

	private Hashtable<String, User> WordQuizzleUsers;  // Mappa il nome utente di un utente registrato al suo oggetto User, contentente altre informazioni (per ora solo password)
	private File json_file;
	
	private ObjectMapper objectMapper = new ObjectMapper();
	
	public Structures() {
		WordQuizzleUsers = new Hashtable<String,User>();
		json_file = new File("WordQuizzleUsers.json");
		
		if(json_file.isFile()) {
			System.out.println("Il file JSON esiste già, recupero tutti gli utenti dal file\n");

			try {
				BufferedReader br = new BufferedReader(new FileReader(json_file));
				
				if (br.readLine() != null) {
					WordQuizzleUsers = objectMapper.readValue(json_file, new TypeReference<Hashtable<String,User>>(){});
					System.out.println("Gli utenti registrati sono: " + WordQuizzleUsers); // Stampo tutto il contenuto della Hashtable dei registrati per vedere se è consistente
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
	
	public void addUser(String _username, User _user) {
		WordQuizzleUsers.put(_username, _user);
	}
	
	public boolean containsUser(String _username) {
		return WordQuizzleUsers.containsKey(_username);
	}
	
	public void writeJson() {
		try {
			objectMapper.writeValue(json_file, WordQuizzleUsers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public User getUser(String _user) {
		return WordQuizzleUsers.get(_user);
	}
	
}
