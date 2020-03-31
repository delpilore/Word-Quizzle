package wordquizzle.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* MATCH
* 
* Questa classe rappresenta una generica partita, composta da due sfidanti, un timer per l'invio del risultato,
* una serie di parole italiane (5) e le traduzioni di esse.
*/

public class Match {
	
	private Opponent firstOpponent;							// primo sfidante (vedere "Opponent")
	private Opponent secondOpponent;						// secondo sfidante (vedere "Opponent")
	private ReschedulableTimer timer;						// timer per l'invio del risultato (vedere "ReschedulableTimer")
															// prenderà come task un oggetto di tipo MatchTimeOver (vedere "MatchTimeOver")
	
	private Hashtable<String,String> wordsTraduction;		// traduzione delle 5 parole
	private ArrayList<String> italianWords;					// 5 parole italiane
    
	// Costruttore
	public Match(String _firstOpponent, int _firstOpponentUDPPort, String _secondOpponent, int _secondOpponentUDPPort, ArrayList<String> _selectedWords ) {	
		firstOpponent = new Opponent(_firstOpponent, _firstOpponentUDPPort);	
		secondOpponent = new Opponent(_secondOpponent, _secondOpponentUDPPort);
		wordsTraduction = new Hashtable<String,String>();
		italianWords = _selectedWords;		
	}
	
	// fetchTraductions()
	//
	// Questo metodo si occupa di recuperare, attraverso 5 richieste HTTP GET ad un servizio, le traduzioni delle 5 parole italiane contenute in italianWords
	public void fetchTraductions() {		
		try {		
			String url = "https://api.mymemory.translated.net/get";
			String charset = "UTF-8";  

			// Per ogni parola in italianWords faccio una richiesta HTTP GET al servizio
			for(int i = 0; i<5; i++) {
				// Prendo la parola italiana di indice i
				String q = italianWords.get(i);
				String langpair = "it|en";
				
				// Costruisco la query per il servizio
				String query = String.format("q=%s&langpair=%s", URLEncoder.encode(q, charset), URLEncoder.encode(langpair, charset));
				
				// Apro la connessione HTTP
				HttpURLConnection con = (HttpURLConnection) new URL(url + "?" + query).openConnection();
				con.setRequestMethod("GET");	
				con.setDoOutput(true);
				
				// In risposta mi aspetto un oggetto Json 
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					ObjectNode object = new ObjectMapper().readValue(inputLine, ObjectNode.class);
					
					// Entro nel campo "responseData" dell'oggetto Json e successivamente nel campo "translatedText"
				    JsonNode node = object.get("responseData");			    
				    node = node.get("translatedText");
				    
				    // Aggiungo alla tabella wordsTraduction la parola italiana e la traduzione corrispondente
				    wordsTraduction.put(italianWords.get(i), node.asText());
				    
				    // Stampa per debug (utile per fare test)
				    System.out.println(wordsTraduction);
				}
				in.close();
				con.disconnect();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// beginMatch()
	//
	// Questo metodo viene invocato al momento dell'inizio di una sfida.
	// Si occupa di inviare ad entrambi gli sfidanti, utilizzando i rispettivi Listener UDP, la prima parola da tradurre.
	public void beginMatch() {
		try {
			byte buf[] = italianWords.get(0).getBytes("UTF8");
			
			InetAddress ip = InetAddress.getLocalHost(); 
			DatagramSocket ds = new DatagramSocket();
			
			DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, firstOpponent.getUDPPort()); 
			DatagramPacket DpSend2 = new DatagramPacket(buf, buf.length, ip, secondOpponent.getUDPPort()); 
			
			ds.send(DpSend); 
			ds.send(DpSend2);
			
			ds.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// sendNextWord(String usr, String _word)
	//
	// Questo metodo viene invocato quando uno sfidante ha risposto e necessita quindi della prossima parola da tradurre
	public void sendNextWord(String usr, String _word) {
		
		if(usr.equals(firstOpponent.getNick())) 
			checkAndSend(firstOpponent, secondOpponent, _word);
		else
			checkAndSend(secondOpponent, firstOpponent, _word);	
		
	}
	
	// checkAndSend(Opponent _opponent, Opponent _second, String _word)
	//
	// Questo metodo è legato al precedente e serve per inviare effettivamente la prossima parola da tradurra ad _opponent.
	// Controlla inoltre se _opponent ha già risposto all'ultima delle 5 parole, chiamando in quel caso endMatch e settando il flag "end" di _opponent a true.
	// Nel caso pure il flag di _second sia true rischedula il timer per l'invio del risultato ad un invio tra 100msec. (altrimenti sarebbero stati i 60sec schedulati inizialmente)
	public void checkAndSend(Opponent _opponent, Opponent _second, String _word) {
		
		// Se la traduzione è corretta aumento le traduzioni corrette di _opponent
		// altrimenti aumento quelle incorrette.
		if (_word.equals(wordsTraduction.get(italianWords.get(_opponent.getCurrentWord())))) {
			_opponent.updateCorrectWords();
		}
		else {
			_opponent.updateIncorrectWords();
		}
		
		// Diminuisco le traduzioni non date e passo al prossimo indice di parola da inviare
		_opponent.updateNotGivenWords();			
		_opponent.updateCurrentWord();
		
		// Se l'indice è ==5 vuol dire che la quinta parola è già stata tradotta e _opponent ha quindi terminato la
		// sua parte di sfida
		if(_opponent.getCurrentWord()==5) {
			// Chiamo il metodo endMatch (subito sotto) per comunicare a _opponent che le parole sono terminate
			endMatch(_opponent.getUDPPort());
			_opponent.end();
			// Se anche _second ha finito mando "subito" (100msec) il risultato ad entrambi (vedere "MatchTimeOver" per dettagli sull'invio del risultato)
			if(_second.hasEnded()) {
				timer.reschedule(100);
			}		
			return;
		}

		// Nel caso ci siano ancora parole da inviare, gli invio la sua prossima sempre in UDP, sfruttando il suo Listener come destinatario
		try {
			byte buf[] = italianWords.get(_opponent.getCurrentWord()).getBytes("UTF8");
			InetAddress ip = InetAddress.getLocalHost(); 
			DatagramSocket ds = new DatagramSocket();
			
			DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, _opponent.getUDPPort()); 
 
			ds.send(DpSend); 
			
			ds.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// endMatch(int port)
	//
	// Questo metodo comunica ad un utente che le sue parole sono terminate o che il tempo è scaduto
	public void endMatch(int port) {
		try {
			String fine = "FINE";
			byte buf[] = fine.getBytes("UTF8");
			
			InetAddress ip = InetAddress.getLocalHost(); 
			DatagramSocket ds = new DatagramSocket();
			
			DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, port); 
 
			ds.send(DpSend); 
			
			ds.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Metodi getters e setters
	
	public String getFirstOpponent() {
		return firstOpponent.getNick();
	}

	public int getFirstOpponentUDPPort() {
		return firstOpponent.getUDPPort();
	}

	public String getSecondOpponent() {
		return secondOpponent.getNick();
	}

	public int getSecondOpponentUDPPort() {
		return secondOpponent.getUDPPort();
	}

	public int getFirstOpponentCorrect() {
		return firstOpponent.getCorrectWords();
	}

	public int getFirstOpponentIncorrect() {
		return firstOpponent.getIncorrectWords();
	}

	public int getFirstOpponentNotGiven() {
		return firstOpponent.getNotGivenWords();
	}

	public int getSecondOpponentCorrect() {
		return secondOpponent.getCorrectWords();
	}

	public int getSecondOpponentIncorrect() {
		return secondOpponent.getIncorrectWords();
	}

	public int getSecondOpponentNotGiven() {
		return secondOpponent.getNotGivenWords();
	}

	public Boolean getFirstOpponentEnd() {
		return firstOpponent.hasEnded();
	}

	public Boolean getSecondOpponentEnd() {
		return secondOpponent.hasEnded();
	}
	
	public void setTimer(ReschedulableTimer _timer) {
		timer = _timer;
	}
	
	// Fine metodi getters e setters
}