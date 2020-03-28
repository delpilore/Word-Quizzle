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

	private User firstOpponent;
	private int firstOpponentUDPPort;
	private int firstOpponentWord;
	private int firstOpponentCorrect;
	private int firstOpponentIncorrect;
	private int firstOpponentNotGiven;

	private User secondOpponent;
	private int secondOpponentUDPPort;
	private int secondOpponentWord;
	private int secondOpponentCorrect;
	private int secondOpponentIncorrect;
	private int secondOpponentNotGiven;
	
	private Hashtable<String,String> wordsTraduction;
	private ArrayList<String> words;

	public Match(User _firstOpponent, int _firstOpponentUDPPort, User _secondOpponent, int _secondOpponentUDPPort ) {
		
		firstOpponent = _firstOpponent;
		firstOpponentUDPPort = _firstOpponentUDPPort;
		firstOpponentWord=0;
		firstOpponentCorrect=0;
		firstOpponentIncorrect=0;
		firstOpponentNotGiven=5;

		secondOpponent = _secondOpponent;
		secondOpponentUDPPort = _secondOpponentUDPPort;
		secondOpponentWord=0;
		secondOpponentCorrect=0;
		secondOpponentIncorrect=0;
		secondOpponentNotGiven=5;
		
		wordsTraduction = new Hashtable<String,String>();
	}
	
	public void fetchTraductions(ArrayList<String> _words) {
		
		words = _words;
		
		try {			
			
			String url = "https://api.mymemory.translated.net/get";
			String charset = "UTF-8";  

			for(int i = 0; i<5; i++) {
				
				String q = words.get(i);
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
				    wordsTraduction.put(words.get(i), node.asText());
				}
				in.close();
				con.disconnect();
			}
	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			byte buf[] = words.get(0).getBytes();
			
			InetAddress ip = InetAddress.getLocalHost(); 
			DatagramSocket ds = new DatagramSocket();
			
			DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, firstOpponentUDPPort); 
			DatagramPacket DpSend2 = new DatagramPacket(buf, buf.length, ip, secondOpponentUDPPort); 
			
			ds.send(DpSend); 
			ds.send(DpSend2);
			
			ds.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}
	
	public Boolean sendNextWord(String usr, String _word) {
		
		if(usr.equals(firstOpponent.getUsername())) {

			if (_word.equals(wordsTraduction.get(words.get(firstOpponentWord)))) {
				firstOpponentCorrect = getFirstOpponentCorrect() + 1;
				firstOpponentNotGiven = getFirstOpponentNotGiven() - 1;
			}
			else {
				firstOpponentIncorrect = getFirstOpponentIncorrect() + 1;
				firstOpponentNotGiven = getFirstOpponentNotGiven() - 1;
			}
						
			firstOpponentWord++;
			
			if(firstOpponentWord==5) {
				try {
					String fine = "FINE";
					byte buf[] = fine.getBytes();
					
					InetAddress ip = InetAddress.getLocalHost(); 
					DatagramSocket ds = new DatagramSocket();
					
					DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, firstOpponentUDPPort); 
		 
					ds.send(DpSend); 
					
					ds.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				return false;
			}
				
			byte buf[] = words.get(firstOpponentWord).getBytes();
			
			try {
				InetAddress ip = InetAddress.getLocalHost(); 
				DatagramSocket ds = new DatagramSocket();
				
				DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, firstOpponentUDPPort); 
	 
				ds.send(DpSend); 
				
				ds.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
			return true;
		}

		else {
			
			
			if (_word.equals(wordsTraduction.get(words.get(secondOpponentWord)))) {
				secondOpponentCorrect = getSecondOpponentCorrect() + 1;
				secondOpponentNotGiven = getSecondOpponentNotGiven() - 1;
			}
			else {
				secondOpponentIncorrect = getSecondOpponentIncorrect() + 1;
				secondOpponentNotGiven = getSecondOpponentNotGiven() - 1;
			}
			
			secondOpponentWord++;
			
			if(secondOpponentWord==5) {
				try {
					String fine = "FINE";
					byte buf[] = fine.getBytes();
					
					InetAddress ip = InetAddress.getLocalHost(); 
					DatagramSocket ds = new DatagramSocket();
					
					DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, secondOpponentUDPPort); 
		 
					ds.send(DpSend); 
					
					ds.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				return false;
			}
			
			byte buf[] = words.get(secondOpponentWord).getBytes();
			
			try {
				InetAddress ip = InetAddress.getLocalHost(); 
				DatagramSocket ds = new DatagramSocket();
				
				DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, secondOpponentUDPPort); 
	 
				ds.send(DpSend); 
				
				ds.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			return true;
		}		
	}

	public String getFirstOpponent() {
		return firstOpponent.getUsername();
	}

	public int getFirstOpponentUDPPort() {
		return firstOpponentUDPPort;
	}

	public String getSecondOpponent() {
		return secondOpponent.getUsername();
	}

	public int getSecondOpponentUDPPort() {
		return secondOpponentUDPPort;
	}

	public int getFirstOpponentCorrect() {
		return firstOpponentCorrect;
	}

	public int getFirstOpponentIncorrect() {
		return firstOpponentIncorrect;
	}

	public int getFirstOpponentNotGiven() {
		return firstOpponentNotGiven;
	}

	public int getSecondOpponentCorrect() {
		return secondOpponentCorrect;
	}

	public int getSecondOpponentIncorrect() {
		return secondOpponentIncorrect;
	}

	public int getSecondOpponentNotGiven() {
		return secondOpponentNotGiven;
	}
	
}