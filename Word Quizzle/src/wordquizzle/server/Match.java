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

public class Match {
	
	private Opponent firstOpponent;
	private Opponent secondOpponent;
	private ReschedulableTimer timer;
	
	private Hashtable<String,String> wordsTraduction;
	private ArrayList<String> italianWords;
    
	public Match(String _firstOpponent, int _firstOpponentUDPPort, String _secondOpponent, int _secondOpponentUDPPort, ArrayList<String> _selectedWords ) {
		
		firstOpponent = new Opponent(_firstOpponent, _firstOpponentUDPPort);	
		secondOpponent = new Opponent(_secondOpponent, _secondOpponentUDPPort);
		wordsTraduction = new Hashtable<String,String>();
		italianWords = _selectedWords;
		
	}
	
	public void fetchTraductions() {
		
		try {			
			
			String url = "https://api.mymemory.translated.net/get";
			String charset = "UTF-8";  

			for(int i = 0; i<5; i++) {
				
				String q = italianWords.get(i);
				String langpair = "it|en";
				
				String query = String.format("q=%s&langpair=%s", URLEncoder.encode(q, charset), URLEncoder.encode(langpair, charset));
				
				HttpURLConnection con = (HttpURLConnection) new URL(url + "?" + query).openConnection();
				con.setRequestMethod("GET");	
				con.setDoOutput(true);
				
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					ObjectNode object = new ObjectMapper().readValue(inputLine, ObjectNode.class);
				    JsonNode node = object.get("responseData");
				    node = node.get("translatedText");
				    wordsTraduction.put(italianWords.get(i), node.asText());
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
	
	public void sendNextWord(String usr, String _word) {
		
		if(usr.equals(firstOpponent.getNick())) 
			checkAndSend(firstOpponent, secondOpponent, _word);
		else
			checkAndSend(secondOpponent, firstOpponent, _word);	
		
	}
	
	public void checkAndSend(Opponent _opponent, Opponent _second, String _word) {
		
		if (_word.equals(wordsTraduction.get(italianWords.get(_opponent.getCurrentWord())))) {
			_opponent.updateCorrectWords();
		}
		else {
			_opponent.updateIncorrectWords();
		}
		
		_opponent.updateNotGivenWords();			
		_opponent.updateCurrentWord();
		
		if(_opponent.getCurrentWord()==5) {
			endMatch(_opponent.getUDPPort());
			_opponent.end();
			if(_second.hasEnded()) {
				timer.reschedule(100);
			}		
			return;
		}

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
}