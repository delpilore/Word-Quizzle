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
	
	
	public static ArrayList<String> getWords() {
		
		File file = new File("dizionario.txt");
		ArrayList<String> words = new ArrayList<String>() ;
		
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

		for (String a : words)
			System.out.println(a);
		
	    return words;
	}
	
}