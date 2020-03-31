package wordquizzle.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import wordquizzle.server.structures.ChallengeableUsers;
import wordquizzle.server.structures.CurrentMatches;
import wordquizzle.server.structures.RegisteredUsers;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* MATCHTIMEOVER
* 
* Task preso in carico da un ReschedulableTimer che si occupa di spedire il risultato di una partita
* ai due sfidanti che la giocavano. (dopo 60 secondi dall'inizio della partita, o prima, se entrambi finiscono in un tempo < 60sec)
*/

public class MatchTimeOver implements Runnable {
	
	private ChallengeableUsers challengeableUsers;
	private CurrentMatches currentMatches;
	private Match match;
	private RegisteredUsers registeredUsers;

	// Costruttore
	public MatchTimeOver(CurrentMatches _currentMatches,  Match _match, ChallengeableUsers _challengeableUsers, RegisteredUsers _registeredUsers ) {
		challengeableUsers = _challengeableUsers;
		currentMatches = _currentMatches;
		match = _match;
		registeredUsers = _registeredUsers;
	}
	
	// Metodo run()
	public void run() {
		// Se il primo sfidante non ha terminato (flag end = false) chiamo il metodo endMatch per comunicargli che la sfida è terminata (vedere "Match")
		if(!match.getFirstOpponentEnd())
			match.endMatch(match.getFirstOpponentUDPPort());
		
		// Se il secondo sfidante non ha terminato (flag end = false) chiamo il metodo endMatch per comunicargli che la sfida è terminata (vedere "Match")
		if(!match.getSecondOpponentEnd())
			match.endMatch(match.getSecondOpponentUDPPort());
		
		// Calcolo il punteggio di entrambi gli sfidanti
		int firstOpponentResult = (2)*match.getFirstOpponentCorrect() + (-1)* match.getFirstOpponentIncorrect();
		int secondOpponentResult = (2)*match.getSecondOpponentCorrect() + (-1)* match.getSecondOpponentIncorrect();
				
		// Costruisco le stringhe che comunicheranno il risultato ad entrambi
		String firstResult = new String("Hai tradotto correttamente " + match.getFirstOpponentCorrect() + 
							 		 " parole/a, ne hai sbagliate/a " + match.getFirstOpponentIncorrect() + 
							 				 	 " e non risposto a " + match.getFirstOpponentNotGiven() +
							 				 	 "\nHai totalizzato " + firstOpponentResult + " punti, il tuo avversario ne ha totalizzati " + secondOpponentResult);
			
		String secondResult= new String("Hai tradotto correttamente " + match.getSecondOpponentCorrect() + 
									 " parole/a, ne hai sbagliate/a " + match.getSecondOpponentIncorrect() + 
												 " e non risposto a " + match.getSecondOpponentNotGiven() +
							 				 	 "\nHai totalizzato " + secondOpponentResult + " punti, il tuo avversario ne ha totalizzati " + firstOpponentResult);
		
		// Controllo chi ha vinto la partita per aggiungere un +3 al suo risultato
		if(firstOpponentResult > secondOpponentResult) {
			firstResult = firstResult + "\nHai vinto complimenti! Guadagni 3 punti extra\n";
			secondResult = secondResult + "\nHai perso! Peccato..\n";
			firstOpponentResult = firstOpponentResult + 3;
		}
		else if (firstOpponentResult < secondOpponentResult) {
			firstResult = firstResult + "\nHai perso! Peccato..\n";
			secondResult = secondResult + "\nHai vinto complimenti! Guadagni 3 punti extra\n";
			secondOpponentResult = secondOpponentResult + 3;
		}
		else {
			firstResult = firstResult + "\nE' un pareggio!\n";
			secondResult = secondResult + "\nE' un pareggio!\n";
		}
		
		// Invio i risultati ad entrambi
		try {
			byte buf1[] = firstResult.getBytes();
			byte buf2[] = secondResult.getBytes();
			
			InetAddress ip = InetAddress.getLocalHost(); 
			DatagramSocket ds = new DatagramSocket();
			
			DatagramPacket DpSend = new DatagramPacket(buf1, buf1.length, ip, match.getFirstOpponentUDPPort()); 
			DatagramPacket DpSend2 = new DatagramPacket(buf2, buf2.length, ip, match.getSecondOpponentUDPPort()); 
 
			ds.send(DpSend); 
			ds.send(DpSend2);
			
			ds.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
			
		// Rimetto entrambi gli sfidanti nella tabella degli "sfidabili" visto che non sono più in partita
		challengeableUsers.addChallenger(match.getFirstOpponent(), match.getFirstOpponentUDPPort());
		challengeableUsers.addChallenger(match.getSecondOpponent(), match.getSecondOpponentUDPPort());
		
		// Tolgo il riferimento all'oggetto che rappresentava la loro partita
		currentMatches.removeMatch(match.getFirstOpponent());
		currentMatches.removeMatch(match.getSecondOpponent());
		
		// Update del punteggio di entrambi
		registeredUsers.getUser(match.getFirstOpponent()).updateScore(firstOpponentResult);
		registeredUsers.getUser(match.getSecondOpponent()).updateScore(secondOpponentResult);
		
		// Scrivo il file Json per mantenere persistente il punteggio modificato dopo la partita
		registeredUsers.writeJson();
	}
}