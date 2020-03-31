package wordquizzle.server;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* OPPONENT
* 
* Questa classe rappresenta un generico "Sfidante" in una partita attualmente in corso.
* Contiene tutte le informazioni necessarie alla gestione del suo stato di giocatore.
*/

public class Opponent {

	private String nick;		// username del giocatore
	private int UDPPort;		// porta su cui ascolta il suo listener UDP (per poter inviare parole e risultato finale)
	private int currentWord;	// indice della parola che deve tradurre attualmente
	private int correctWords;	// numero di parole tradotte correttamente
	private int incorrectWords; // numero di parole tradotte in modo sbagliato
	private int notGivenWords;  // numero di risposte non date
	private Boolean end;		// flag che definisce se ha finito o meno la partita
	
	// Costruttore
	public Opponent(String _nick, int _port) {
		nick = _nick;
		UDPPort = _port;
		currentWord = 0;
		correctWords = 0;
		incorrectWords = 0;
		notGivenWords = 5;
		end = false;
	}

	// Metodi getters
	
	public String getNick() {
		return nick;
	}

	public int getUDPPort() {
		return UDPPort;
	}

	public int getCurrentWord() {
		return currentWord;
	}

	public int getCorrectWords() {
		return correctWords;
	}

	public int getIncorrectWords() {
		return incorrectWords;
	}

	public int getNotGivenWords() {
		return notGivenWords;
	}

	public Boolean hasEnded() {
		return end;
	}
	
	// Fine metodi getter
	
	
	// updateCurrentWord()
	//
	// Aggiorna l'indice della parola da mandare allo sfidante
	public void updateCurrentWord() {
		currentWord++;
	}
	
	// updateCorrectWords()
	//
	// Aumenta di 1 il numero di parole tradotte correttamente
	public void updateCorrectWords() {
		correctWords++;
	}
	
	// updateIncorrectWords()
	//
	// Aumenta di 1 il numero di parole tradotte in modo sbagliato
	public void updateIncorrectWords() {
		incorrectWords++;
	}
	
	// updateNotGivenWords()
	//
	// Diminuisce di 1 le risposte non date
	public void updateNotGivenWords() {
		notGivenWords--;
	}
	
	// end()
	//
	// Setta il flag end a true, in modo che sia possibile vedere che questo sfidante
	// ha terminato le sue parole da tradurre
	public void end() {
		end=true;
	}
}