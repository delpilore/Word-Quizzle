package wordquizzle.server.structures;

import java.util.Hashtable;

import wordquizzle.server.Match;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* CURRENTMATCHES
* 
* Questa classe rappresenta le partite attualmente in corso (oggetti "Match") legate agli utenti che le giocano.
* Serve ai worker per recuperare tutte le informazioni relative ad una partita che un utente sta giocando, quando devono gestire 
* la ricezione di una parola da parte di quest'ultimo.
*/

public class CurrentMatches {

	private Hashtable<String, Match> matches;
	
	// Costruttore
	public CurrentMatches() {
		matches = new Hashtable<String, Match>();
	}
	
	// addMatch(String usr, Match match)
	//
	// Aggiunge una coppia <usr, match> alla struttura, significa che usr ha iniziato una partita
	public void addMatch(String usr, Match match) {
		matches.put(usr,match);
	}
	
	// getMatch(String usr)
	//
	// Restuisce la partita in cui sta giocando usr
	public Match getMatch(String usr) {
		return matches.get(usr);
	}
	
	// removeMatch(String _user)
	//
	// Rimuove la partita in cui stava giocando usr (metodo chiamato quando la partita si conclude)
	public void removeMatch(String _user) {
		matches.remove(_user);
	}
}