package source;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestHandler implements Runnable {
	
	private Socket client;
	private Structures WordQuizzleUsers;
	
	private Request request;
	private Response response;
	
	private LinkedBlockingQueue<Socket> requestQueue;
	
	public RequestHandler(Structures _support) {
		WordQuizzleUsers = _support;
		requestQueue = WordQuizzleUsers.getRequestsQueue();
	}
	
	public void run() {
		
		while(true) {
			
			synchronized(requestQueue) {
				while(requestQueue.isEmpty()) {
					try {
						requestQueue.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			client = requestQueue.poll();
			
			try {
				
				request = (Request) Utility.read(client);
				
				String usr = request.getRequestUsername();
				String pass = request.getRequestPassword();
				String message = request.getRequestMessage();
	
				switch (request.getRequestCommand()) {
					case LOGIN:
						
						if (!WordQuizzleUsers.containsUser(usr)) {
							response = new Response (StatusCodes.USERNOTREGISTERED);
							Utility.write(client,response);
			        		client.close();
						}
						
						else if (WordQuizzleUsers.getUser(usr).getOnlineState()) {
							response = new Response (StatusCodes.USERALREADYONLINE);
							Utility.write(client,response);
			        		client.close();
						}
						
						else if (!pass.equals(WordQuizzleUsers.getUser(usr).getPassword())) {
							response = new Response (StatusCodes.WRONGPASSWORD);
							Utility.write(client,response);
			        		client.close();
						}
						
						else {
							response = new Response (StatusCodes.OK);
																		
							Utility.write(client,response);
							
							// Se il login ha successo mi aspetto che il client mi invii la porta dove ascolta il suo
							// listener UDP, lo aggiungo ai Challengers (è come se fosse online)
							int port = (int) Utility.read(client);
							WordQuizzleUsers.addChallenger(usr, port);
							WordQuizzleUsers.getUser(usr).setOnlineState(true);
							
							synchronized(requestQueue) {
								requestQueue.add(client);
								requestQueue.notify();
							}
							
						}
					
		        	break;
		        	
					case LOGOUT:
	
						response = new Response (StatusCodes.OK);
						// Tolgo anche l'occorrenza dalla tabella dei challengers
						WordQuizzleUsers.removeChallenger(usr);
						WordQuizzleUsers.getUser(usr).setOnlineState(false);
						
						Utility.write(client,response);
		        		
		        		client.close();
		        		
		        	break;
		        	
					case ADDFRIEND:
						
						if (!WordQuizzleUsers.containsUser(message)) {
							response = new Response (StatusCodes.WRONGREQUEST);
							Utility.write(client,response);
						}
						else if (WordQuizzleUsers.getUser(usr).isFriend(message)){
							response = new Response (StatusCodes.ALREADYFRIENDS);
							Utility.write(client,response);
						}
						else if (usr.equals(message)){
							response = new Response (StatusCodes.SELFREQUEST);
							Utility.write(client,response);
						}
						else {
							response = new Response (StatusCodes.OK);
							WordQuizzleUsers.getUser(usr).addFriend(message);
							WordQuizzleUsers.getUser(message).addFriend(usr);

							Utility.write(client,response);
							
							WordQuizzleUsers.writeJson();
						}
						
						synchronized(requestQueue) {
							requestQueue.add(client);
							requestQueue.notify();
						}
		        	break;
		        	
					case FRIENDLIST:
						
						ObjectMapper mapper = new ObjectMapper();
						JsonNode array = mapper.valueToTree(WordQuizzleUsers.getUser(usr).getFriendList());
						
						Utility.write(client,array);
						
						synchronized(requestQueue) {
							requestQueue.add(client);
							requestQueue.notify();
						}
		        	break;
		        	
					case SCORE:
						
						Utility.write(client,WordQuizzleUsers.getUser(usr).getScore());
						
						synchronized(requestQueue) {
							requestQueue.add(client);
							requestQueue.notify();
						}
					
					break;
					
					case RANKING:
						
						JsonNode rankingTable = Utility.getRanking(WordQuizzleUsers, usr);
						
						Utility.write(client,rankingTable);
						
						synchronized(requestQueue) {
							requestQueue.add(client);
							requestQueue.notify();
						}
		        	break;
		        	
					case CHALLENGEFRIEND:
						
						if (!WordQuizzleUsers.containsUser(message)) {
							response = new Response (StatusCodes.WRONGREQUEST);
							Utility.write(client,response);
						}
						else if (!WordQuizzleUsers.getUser(message).getOnlineState()) {
							response = new Response (StatusCodes.USERNOTONLINE);
							Utility.write(client,response);
						}
						else if (!WordQuizzleUsers.containsChallenger(message)) {
							response = new Response (StatusCodes.USERINMATCH);
							Utility.write(client,response);
						}
						else if (usr.equals(message)){
							response = new Response (StatusCodes.SELFREQUEST);
							Utility.write(client,response);
						}
						else if (!WordQuizzleUsers.getUser(message).isFriend(usr)){
							response = new Response (StatusCodes.NOTFRIENDS);
							Utility.write(client,response);
						}	
						else {
							
							ArrayList<String> selectedWords = Utility.getWords();
							
							byte buf[] = usr.getBytes();
							byte[] receive = new byte[100]; 
							
							InetAddress ip = InetAddress.getLocalHost(); 
							DatagramSocket ds = new DatagramSocket();
							
							DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, WordQuizzleUsers.getChallenger(message)); 
							DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
							
							ds.send(DpSend); 
				            ds.receive(DpReceive);

				            if(Utility.data(receive).toString().equals("y")) {
								response = new Response (StatusCodes.MATCHSTARTING);
								Utility.write(client,response);
								
								Match match = new Match(usr,message,WordQuizzleUsers);
								match.fetchTraductions(selectedWords);
								
								WordQuizzleUsers.addMatch(usr, match);
								WordQuizzleUsers.addMatch(message, match);
								
								WordQuizzleUsers.removeChallenger(usr);
								WordQuizzleUsers.removeChallenger(message);
				            }
				            else if (Utility.data(receive).toString().equals("n")){
								response = new Response (StatusCodes.MATCHDECLINED);
								Utility.write(client,response);
				            }
				            else {
				            	response = new Response (StatusCodes.TIMEOUT);
								Utility.write(client,response);
				            }
				            
				            ds.close();
				            	
						}
						
						synchronized(requestQueue) {
							requestQueue.add(client);
							requestQueue.notify();
						}
					
					break;
					
					case MATCH:
						
						Match match = WordQuizzleUsers.getMatch(usr);
						if(!match.sendNextWord(usr,message)) {
							WordQuizzleUsers.addChallenger(match.getFirstOpponent(), match.getFirstOpponentUDPPort());
							WordQuizzleUsers.addChallenger(match.getSecondOpponent(), match.getSecondOpponentUDPPort());
							
							WordQuizzleUsers.removeMatch(usr);
							WordQuizzleUsers.removeMatch(message);
						}
						
						synchronized(requestQueue) {
							requestQueue.add(client);
							requestQueue.notify();
						}
					break;
		        	
				default:
					break;
					        
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}
}