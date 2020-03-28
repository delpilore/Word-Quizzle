package source;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* UTILITY 
* 
* Questa classe si limita a contenere metodi utili al funzionamento del server.
* Perlopiù si tratta di metodi che risulterebbero "pesanti" da vedere ripetuti, o soltanto scritti,
* nel codice effettivo del server.
*/

public class Utility {
	
	// write(Socket receiver, Object message)
	//
	// Questo metodo racchiude tutto il necessario per spedire un oggetto su una socket.
	// Viene usato sia da Client che da Server.
	public static void write(Socket receiver, Object message) {
		try {
			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(receiver.getOutputStream()));
			writer.writeObject(message);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// read()
	//
	// Questo metodo racchiude tutto il necessario per leggere un oggetto da una socket.
	// Viene usato sia da Client che da Server.
	public static Object read(Socket sender) {
		try {
			ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(sender.getInputStream()));
			return reader.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
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
	public static JsonNode getRanking(Structures _support, String usr) {
		
		// ArrayList per la classifica
		ArrayList<User> Ranking = new ArrayList<User>();
		// ArrayList delle amicizie di usr
		ArrayList<String> friendList = _support.getUser(usr).getFriendList();
		
		// Inizio aggiungendo usr alla classifica "Ranking"
		Ranking.add(_support.getUser(usr));
		
		// Segue l'aggiunta di tutti gli amici di usr alla classifica "Ranking"
		for (String friend : friendList) {
			Ranking.add(_support.getUser(friend));
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
	
	public static int portScanner() {
		for (int i=1024; i<49151; i++){
			try (DatagramSocket s =new DatagramSocket(i)){ 
				return i;
			}
			catch (BindException e) {
				;
			}
			catch (Exception e) {
				System.out.println (e);
			}
		} 
		return 0;
	}
	
	public static StringBuilder data(byte[] a) 
    { 
        if (a == null) 
            return null; 
        StringBuilder ret = new StringBuilder(); 
        int i = 0; 
        while (a[i] != 0) 
        { 
            ret.append((char) a[i]); 
            i++; 
        } 
        return ret; 
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