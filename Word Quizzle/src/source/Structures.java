package source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
 * STRUCTURES (oggetto istanziato una sola volta in "WQServer" e condiviso tra tutti i thread del Server)
 * 
 * Questa classe contiene tutte le strutture utili al funzionamento del Server, insieme ai relativi metodi.
 */

public class Structures {
	
	// Struttura principale del Server.
	// Mappa il nome utente di un utente registrato, al suo oggetto User, che lo definisce completamente (vedere "User")
	private Hashtable<String, User> WordQuizzleUsers; 
	
	// File JSON dove verrà salvata la HashTable precedente.
	// Questo per mantenere persistente lo stato del server riguardo gli utenti registrati al servizio.
	private File json_file;
	
	// Classe interna al package Jackson per la gestione degli oggetti JSON (verrà utilizzata nel costruttore e nel metodo writeJSON)
	private ObjectMapper objectMapper;
	
	// Coda di socket accettati dal Listener o attualmente loggati al servizio 
	private LinkedBlockingQueue<Socket> activeRequests;
	
	private Hashtable<String, Integer> WordQuizzleChallengers;
	
	// Structures()
	//
	// Il metodo costruttore è senz'altro il metodo più importante di questa classe.
	// Esso, come detto più volte, viene chiamato una volta sola, all'atto dell'unica istanziazione 
	// della seguente classe da parte di WQServer. (che condividerà poi l'oggetto a tutti i thread del server)
	// Si occupa di istanziare WordQuizzleUsers, json_file e activeRequests.	
	// Fatto ciò, controlla se esiste già un file JSON da cui recuperare lo stato precedente del server.
	// In caso positivo, attraverso objectMapper e metodi offerti dal package Jackson, si preoccupa di riportare
	// la HashTable WordQuizzleUsers allo stato rappresentato dal file JSON.
	// In caso contrario procede ad un avvio pulito del server.
	public Structures() {
		
		WordQuizzleUsers = new Hashtable<String,User>();
		json_file = new File("WordQuizzleUsers.json");
		activeRequests = new LinkedBlockingQueue<Socket>();
		WordQuizzleChallengers = new Hashtable<String,Integer>();
		
		// Istanzio l'ObjectMapper, che ci servirà per scrivere WordQuizzleUsers sul file json_file (e viceversa) 
		// Inoltre attivo la stampa indentata su di esso (per maggiore leggibilità)
		objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		if(json_file.isFile()) {
			System.out.println("Il file JSON esiste già, recupero tutti gli utenti dal file\n");

			try {
				BufferedReader br = new BufferedReader(new FileReader(json_file));
				
				if (br.readLine() != null) {
					// Recupero WordQuizzleUsers (stato precedente del server) attraverso il file json_file e
					// stampo tutto il contenuto di essa per vedere se è consistente
					WordQuizzleUsers = objectMapper.readValue(json_file, new TypeReference<Hashtable<String,User>>(){});
					System.out.println("Gli utenti registrati sono: " + WordQuizzleUsers); 
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
		
	// addUser(String _username, User _user)
	//
	// Questo metodo si limita ad aggiungere un utente, rappresentato dalla coppia <key: String username, value: Oggetto User relativo>,
	// alla HashTable WordQuizzleUsers. Viene chiamato solamente all'atto di una registrazione. (vedere "RegisterImpl")
	// Di conseguenza la tabella WordQuizzleUsers conterrà tutti e soli gli utenti registrati al servizio.
	public void addUser(String _username, User _user) {
		WordQuizzleUsers.put(_username, _user);
	}
	
	// containsUser(String _username)
	//
	// Questo metodo restituisce un valore true o false a seconda che l'utente con l'username passato sia presente o meno
	// nella HashTable WordQuizzleUsers, ovvero se sia registrato o no.
	public boolean containsUser(String _username) {
		return WordQuizzleUsers.containsKey(_username);
	}
	
	// writeJson()
	//
	// Questo metodo scrive in output sul file JSON, dichiarato e istanziato inizialmente, tutto il contenuto di 
	// WordQuizzleUsers. In questo modo, la persistenza verrà garantita.
	// Viene chiamato ogni qualvolta avviene un aggiornamento riguardo ad un utente registrato al servizio. 
	// (passa da online a offline, cambia il suo punteggio, aggiunge un amico)
	public void writeJson() {
		try {
			objectMapper.writeValue(json_file, WordQuizzleUsers);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// getUser(String _user)
	//
	// Metodo che restituisce l'oggetto User associato al nome utente passato per argomento
	public User getUser(String _user) {
		return WordQuizzleUsers.get(_user);
	}
	
	// getRequestsQueue()
	//
	// Metodo che restituisce la LinkedBlockingQueue dei socket accettati/da gestire
	public LinkedBlockingQueue<Socket> getRequestsQueue() {
		return activeRequests;
	}
	
	public void addChallenger(String _user, int _port) {
		WordQuizzleChallengers.put(_user,_port);
	}
	
	public int getChallenger(String _user) {
		return WordQuizzleChallengers.get(_user);
	}
	
	public void removeChallenger(String _user) {
		WordQuizzleChallengers.remove(_user);
	}
}