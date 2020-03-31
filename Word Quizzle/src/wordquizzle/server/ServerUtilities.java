package wordquizzle.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import wordquizzle.server.structures.RegisteredUsers;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* SERVERUTILITIES
* 
* Questa classe contiene le implementazioni di metodi considerati d'utilità per il server.
* In questo caso abbiamo due metodi: "getRanking" che si occupa di restituire un oggetto JsonNode
* contentente la classica ORDINATA degli amici dell'utente passato per argomento (compreso anche lui)
* e getWords() che si occupa di estrarre 5 parole da un file txt chiamato "dizionario.txt" da utilizzare
* nel corso di una partita.
*/

public class ServerUtilities {
	
	// getRanking(Structures _support, String usr)
	//
	// Questo metodo si occupa di costruire un ArrayList di User ordinati per punteggio e successivamente 
	// utilizzarlo per scrivere una classfica su un oggetto JsonNode restituito in output.
	// La classifica sarà costituita da una serie di campi in formato JSON
	//	{
	// 		user1: score1
	// 		user2: score2
	// 		ecc..
	//	}
	public static JsonNode getRanking(RegisteredUsers registeredUsers, String usr) {
		
		// ArrayList per la classifica
		ArrayList<User> Ranking = new ArrayList<User>();
		// ArrayList delle amicizie di usr
		ArrayList<String> friendList = registeredUsers.getUser(usr).getFriendList();
		
		// Inizio aggiungendo usr alla classifica "Ranking"
		Ranking.add(registeredUsers.getUser(usr));
		
		// Segue l'aggiunta di tutti gli amici di usr alla classifica "Ranking"
		for (String friend : friendList) {
			Ranking.add(registeredUsers.getUser(friend));
		}
		
		// Viene ordinata la classifica "Ranking" attraverso il metodo sort e il comparatore ScoreComparator (vedere file relativo)
		// che si limita a comparare gli oggetti User a seconda del loro campo score.
		Collections.sort(Ranking,Collections.reverseOrder(new ScoreComparator()));
		
		// Viene infine scritto tutto in un JsonNode e ritornato
		ObjectMapper rankingMapper = new ObjectMapper();
		JsonNode rankingTable = rankingMapper.createObjectNode();
		for (User friend : Ranking) {
			((ObjectNode) rankingTable).put(friend.getUsername(), friend.getScore());
		}

		return rankingTable;
	}
	
	// getWords()
	//
	// Questo metodo si occupa di estrarre 5 parole da un file txt chiamato dizionario.txt e inserirle 
	// in un ArrayList<String> da ritornare successivamente.
	// Queste 5 parole saranno relative ad una sfida tra due utenti.
	public static ArrayList<String> getWords() {
		
		File file = new File("dizionario.txt");
		ArrayList<String> words = new ArrayList<String>() ;
		
		// Il metodo con cui vengono estratte queste 5 parole è un metodo randomico. (che cicla sul file finche non sono state trovate 5 parole)
		// Passando in rassegna tutte le parole (una per linea), una in particolare viene presa solo quando
		// una variabile random (rand), che può assumere valori in un range 0-80, assume il valore 40. 
		// Quindi ogni parola ha 1/80 di probabilità di essere scelta. (nella relazione esploro la ridotta probabilità che questo metodo non ritorni 5 parole)
		Random rand = new Random();
		try {
			Scanner sc = new Scanner(file,"UTF-8");
		    while(words.size()<5)
		    {
		       String line = sc.nextLine();
		       if(rand.nextInt(80) == 40)
		          words.add(line);     
		    }
		     
		    sc.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		// Ritorno l'ArrayList<String> costruito sopra
	    return words;
	}
}