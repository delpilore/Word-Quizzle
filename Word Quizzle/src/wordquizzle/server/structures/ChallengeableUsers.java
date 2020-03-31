package wordquizzle.server.structures;

import java.util.concurrent.ConcurrentHashMap;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* CHALLANGEABLEUSERS
* 
* Questa classe rappresenta gli utenti "sfidabili", legati alla porta su cui ascolta il loro Listener UDP.
* "Sfidabili" significa che sono utenti online e NON sono in partita o NON hanno richieste di sfida pendenti.
*/

public class ChallengeableUsers {
	
	private ConcurrentHashMap<String, Integer> challengers;
	
	// Costruttore
	public ChallengeableUsers() {
		challengers = new ConcurrentHashMap<String, Integer>();
	}
	
	// addChallenger(String _user, int _port)
	//
	// Aggiunge un utente sfidabile 
	public void addChallenger(String _user, int _port) {
		challengers.put(_user,_port);
	}
	
	// getChallengerPort(String _user)
	//
	// Restituisce la porta su cui ascolta il listener UDP dell'utente _user
	public int getChallengerPort(String _user) {
		return challengers.get(_user);
	}
	
	// removeChallenger(String _user)
	//
	// Rimuove _user dagli utenti sfidabili (perché magari ha effettuato un logout, è entrato in una sfida o ha una richiesta di sfida pendente)
	public void removeChallenger(String _user) {
		challengers.remove(_user);
	}
	
	// isChallengeable(String _user)
	//
	// Restituisce true se _user è sfidabile, quindi se è online e non in partita o con richieste di sfida pendenti, false altrimenti.
	public Boolean isChallengeable(String _user) {
		return challengers.containsKey(_user);
	}
}