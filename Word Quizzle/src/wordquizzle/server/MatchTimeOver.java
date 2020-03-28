package wordquizzle.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TimerTask;

import wordquizzle.server.structures.ChallengeableUsers;
import wordquizzle.server.structures.CurrentMatches;

public class MatchTimeOver extends TimerTask {
	
	private ChallengeableUsers challengeableUsers;
	private CurrentMatches currentMatches;
	private Match match;

	public MatchTimeOver(CurrentMatches _currentMatches,  Match _match, ChallengeableUsers _challengeableUsers ) {
		challengeableUsers = _challengeableUsers;
		currentMatches = _currentMatches;
		match = _match;
	}
	
	public void run() {
		if(!match.getFirstOpponentEnd())
			match.endMatch(match.getFirstOpponentUDPPort());
		if(!match.getSecondOpponentEnd())
			match.endMatch(match.getSecondOpponentUDPPort());
		
		int firstOpponentResult = (2)*match.getFirstOpponentCorrect() + (-1)* match.getFirstOpponentIncorrect();
		int secondOpponentResult = (2)*match.getSecondOpponentCorrect() + (-1)* match.getSecondOpponentIncorrect();
				
		String firstResult = new String("Hai tradotto correttamente " + match.getFirstOpponentCorrect() + 
							 		 " parole/a, ne hai sbagliate/a " + match.getFirstOpponentIncorrect() + 
							 				 	 " e non risposto a " + match.getFirstOpponentNotGiven() +
							 				 	 "\nHai totalizzato " + firstOpponentResult + " punti, il tuo avversario ne ha totalizzati " + secondOpponentResult);
			
		String secondResult= new String("Hai tradotto correttamente " + match.getSecondOpponentCorrect() + 
									 " parole/a, ne hai sbagliate/a " + match.getSecondOpponentIncorrect() + 
												 " e non risposto a " + match.getSecondOpponentNotGiven() +
							 				 	 "\nHai totalizzato " + secondOpponentResult + " punti, il tuo avversario ne ha totalizzati " + firstOpponentResult);
		
		if(firstOpponentResult > secondOpponentResult) {
			firstResult = firstResult + "\nHai vinto complimenti! Guadagni 3 punti extra\n";
			secondResult = secondResult + "\nHai perso! Peccato..\n";
		}
		else if (firstOpponentResult < secondOpponentResult) {
			firstResult = firstResult + "\nHai perso! Peccato..\n";
			secondResult = secondResult + "\nHai vinto complimenti! Guadagni 3 punti extra\n";
		}
		else {
			firstResult = firstResult + "\nE' un pareggio!\n";
			secondResult = secondResult + "\nE' un pareggio!\n";
		}
		
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
			
		challengeableUsers.addChallenger(match.getFirstOpponent(), match.getFirstOpponentUDPPort());
		challengeableUsers.addChallenger(match.getSecondOpponent(), match.getSecondOpponentUDPPort());
		
		currentMatches.removeMatch(match.getFirstOpponent());
		currentMatches.removeMatch(match.getSecondOpponent());
		
		//devo anche updatare il punteggioooo
	}
}