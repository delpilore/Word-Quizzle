package source;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Utility {
	public static void write(Socket receiver, Object request) {
		try {
			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(receiver.getOutputStream()));
			writer.writeObject(request);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Object read(Socket sender) {
		try {
			ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(sender.getInputStream()));
			return reader.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static JsonNode getRanking(Structures _support, String usr) {
		ArrayList<User> Ranking = new ArrayList<User>();
		ArrayList<String> friendList = _support.getUser(usr).getFriendList();
		
		Ranking.add(_support.getUser(usr));
		
		for (String friend : friendList) {
			Ranking.add(_support.getUser(friend));
		}
		
		Collections.sort(Ranking,Collections.reverseOrder(new ScoreComparator()));
		
		ObjectMapper rankingMapper = new ObjectMapper();
		JsonNode rankingTable = rankingMapper.createObjectNode();
		
		for (User friend : Ranking) {
			((ObjectNode) rankingTable).put(friend.getUsername(), friend.getScore());
		}

		return rankingTable;
	}
}