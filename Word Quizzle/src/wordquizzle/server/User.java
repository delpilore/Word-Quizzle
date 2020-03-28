package wordquizzle.server;

import java.util.ArrayList;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* USER
* 
* Questa classe rappresenta un generico User registrato al servizio Word Quizzle.
* Il costruttore viene chiamato solo all'atto dell'avvenuta registrazione di un utente (vedere "RegisterImpl")
*/

public class User {
	
	// Variabili di stato che rappresentano un utente iscritto a Word Quizzle
	private String username;
	private String password;
	private ArrayList<String> FriendList;
	private int score;
	private Boolean onlineState;

	// User(String _username, String _password)
	//
	// Setta username e password dell'oggetto ai relativi _username e _password passati per argomento.
	// La sua friendlist è vuota e il suo score è 0
	public User(String _username, String _password) {
		setUsername(_username);
		setPassword(_password);
		setFriendList(new ArrayList<String>());
		setScore(0);
		setOnlineState(false);
	}
		
	// Costruttore senza argomenti (necessario per il funzionamento del pacchetto Jackson per la scrittura del file JSON)
	public User() {
		setUsername(null);
		setPassword(null);
		setFriendList(null);
		setScore(0);
		setOnlineState(false);
	}
	
	// INIZIO Getters e Setters di tutte le variabili di stato

	private void setUsername(String username) {
		this.username = username;
	}
	
	public String getUsername() {
		return username;
	}
	
	private void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}
	
	public void setFriendList(ArrayList<String> _friendList) {
		this.FriendList = _friendList;
	}

	public ArrayList<String> getFriendList() {
		return FriendList;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setOnlineState(Boolean onlineState) {
		this.onlineState = onlineState;
	}
	
	public Boolean getOnlineState() {
		return onlineState;
	}

	// FINE Getters e Setters di tutte le variabili di stato
	
	// addFriend(String _user)
	//
	// Aggiunge _user alla lista amici di this
	public void addFriend(String _user) {
		FriendList.add(_user);
	}
	
	// isFriend(String _user)
	//
	// Ritorna true o false a seconda che _user sia o meno amico di this
	public boolean isFriend(String _user) {
		return FriendList.contains(_user);
	}

	// updateScore(int _matchscore)
	//
	// Aggiunge allo score di this il punteggio ottenuto nella partita giocata
	public void updateScore(int _matchscore) {
		score = score + _matchscore;
	}

}