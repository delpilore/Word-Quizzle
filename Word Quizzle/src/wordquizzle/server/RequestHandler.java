package wordquizzle.server;

import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import wordquizzle.StatusCodes;
import wordquizzle.server.structures.ChallengeableUsers;
import wordquizzle.server.structures.CurrentMatches;
import wordquizzle.server.structures.RegisteredUsers;
import wordquizzle.Request;
import wordquizzle.Response;
import wordquizzle.GeneralUtilities;
import wordquizzle.Comunication;

public class RequestHandler implements Runnable {
	
	private Socket client;
	
	private RegisteredUsers registeredUsers;
	private ChallengeableUsers challengeableUsers;
	private CurrentMatches currentMatches;
	private LinkedBlockingQueue<Socket> activeRequests;
	
	private Request request;
	private Response response;
	
	public RequestHandler(RegisteredUsers _registeredUsers, ChallengeableUsers _challengeableUsers, CurrentMatches _currentMatches, LinkedBlockingQueue<Socket> _activeRequests) {
		registeredUsers = _registeredUsers;
		challengeableUsers = _challengeableUsers;
		currentMatches = _currentMatches;
		activeRequests = _activeRequests;
	}
	
	public void run() {
		
		while(true) {
			
			try {		
				client = activeRequests.take();
				
				ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
				request = (Request) reader.readObject();

				String usr = request.getRequestUsername();
				String pass = request.getRequestPassword();
				String message = request.getRequestMessage();
	
				switch (request.getRequestCommand()) {
					case LOGIN:
						
						if (!registeredUsers.isRegistered(usr)) {
							response = new Response (StatusCodes.USERNOTREGISTERED);
							Comunication.write(client,response);
			        		client.close();
						}
						
						else if (registeredUsers.getUser(usr).getOnlineState()) {
							response = new Response (StatusCodes.USERALREADYONLINE);
							Comunication.write(client,response);
			        		client.close();
						}
						
						else if (!pass.equals(registeredUsers.getUser(usr).getPassword())) {
							response = new Response (StatusCodes.WRONGPASSWORD);
							Comunication.write(client,response);
			        		client.close();
						}
						
						else {
							response = new Response (StatusCodes.OK);
																		
							Comunication.write(client,response);
							
							// Se il login ha successo mi aspetto che il client mi invii la porta dove ascolta il suo
							// listener UDP, lo aggiungo ai Challengers (è come se fosse online)
							int port = (int) Comunication.read(client);
							challengeableUsers.addChallenger(usr, port);
							registeredUsers.getUser(usr).setOnlineState(true);
							
							activeRequests.offer(client);
						}
					
		        	break;
		        	
					case LOGOUT:
	
						response = new Response (StatusCodes.OK);
						// Tolgo anche l'occorrenza dalla tabella dei challengers
						challengeableUsers.removeChallenger(usr);
						registeredUsers.getUser(usr).setOnlineState(false);
						
						Comunication.write(client,response);
		        		
		        		client.close();
		        		
		        	break;
		        	
					case ADDFRIEND:
						
						if (!registeredUsers.isRegistered(message)) {
							response = new Response (StatusCodes.WRONGREQUEST);
							Comunication.write(client,response);
						}
						else if (registeredUsers.getUser(usr).isFriend(message)){
							response = new Response (StatusCodes.ALREADYFRIENDS);
							Comunication.write(client,response);
						}
						else if (usr.equals(message)){
							response = new Response (StatusCodes.SELFREQUEST);
							Comunication.write(client,response);
						}
						else {
							response = new Response (StatusCodes.OK);
							registeredUsers.getUser(usr).addFriend(message);
							registeredUsers.getUser(message).addFriend(usr);

							Comunication.write(client,response);
							
							registeredUsers.writeJson();
						}
						
						activeRequests.offer(client);
						
		        	break;
		        	
					case FRIENDLIST:
						
						ObjectMapper mapper = new ObjectMapper();
						JsonNode array = mapper.valueToTree(registeredUsers.getUser(usr).getFriendList());
						
						Comunication.write(client,array);
						
						activeRequests.offer(client);
						
		        	break;
		        	
					case SCORE:
						
						Comunication.write(client,registeredUsers.getUser(usr).getScore());
						
						activeRequests.offer(client);
					
					break;
					
					case RANKING:
						
						JsonNode rankingTable = ServerUtilities.getRanking(registeredUsers, usr);
						
						Comunication.write(client,rankingTable);
						
						activeRequests.offer(client);
		        	break;
		        	
					case CHALLENGEFRIEND:
						
						if (!registeredUsers.isRegistered(message)) {
							response = new Response (StatusCodes.WRONGREQUEST);
							Comunication.write(client,response);
						}
						else if (!registeredUsers.getUser(message).isFriend(usr)){
							response = new Response (StatusCodes.NOTFRIENDS);
							Comunication.write(client,response);
						}	
						else if (!registeredUsers.getUser(message).getOnlineState()) {
							response = new Response (StatusCodes.USERNOTONLINE);
							Comunication.write(client,response);
						}
						else if (!challengeableUsers.isChallengeable(message)) {
							response = new Response (StatusCodes.USERINMATCH);
							Comunication.write(client,response);
						}
						else if (usr.equals(message)){
							response = new Response (StatusCodes.SELFREQUEST);
							Comunication.write(client,response);
						}
						else {
							
							ArrayList<String> selectedWords = ServerUtilities.getWords();
							
							byte buf[] = usr.getBytes();
							byte[] receive = new byte[100]; 
							
							InetAddress ip = InetAddress.getLocalHost(); 
							DatagramSocket ds = new DatagramSocket();
							
							DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, challengeableUsers.getChallengerPort(message)); 
							DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 
							
							ds.send(DpSend); 
				            ds.receive(DpReceive);

				            if(GeneralUtilities.data(receive).toString().equals("y")) {
								response = new Response (StatusCodes.MATCHSTARTING);
								Comunication.write(client,response);
								
								Match match = new Match(registeredUsers.getUser(usr), challengeableUsers.getChallengerPort(usr), registeredUsers.getUser(message), challengeableUsers.getChallengerPort(message));
								match.fetchTraductions(selectedWords);
								
								currentMatches.addMatch(usr, match);
								currentMatches.addMatch(message, match);
								
								challengeableUsers.removeChallenger(usr);
								challengeableUsers.removeChallenger(message);
				            }
				            else if (GeneralUtilities.data(receive).toString().equals("n")){
								response = new Response (StatusCodes.MATCHDECLINED);
								Comunication.write(client,response);
				            }
				            else {
				            	response = new Response (StatusCodes.TIMEOUT);
								Comunication.write(client,response);
				            }
				            
				            ds.close();
				            	
						}
						
						activeRequests.offer(client);
					
					break;
					
					case MATCH:
						
						Match match = currentMatches.getMatch(usr);
						if(!match.sendNextWord(usr,message)) {
							String result;
							if(usr.equals(match.getFirstOpponent())) {
								result = new String("Hai tradotto correttamente " + match.getFirstOpponentCorrect() + 
												 " parole/a, ne hai sbagliate/a " + match.getFirstOpponentIncorrect() + 
															 " e non risposto a " + match.getFirstOpponentNotGiven());
								
								registeredUsers.getUser(usr).updateScore((2)*match.getFirstOpponentCorrect() + (-1)* match.getFirstOpponentIncorrect());
							}
							else {
								result= new String("Hai tradotto correttamente " + match.getSecondOpponentCorrect() + 
												" parole/a, ne hai sbagliate/a " + match.getSecondOpponentIncorrect() + 
															" e non risposto a " + match.getSecondOpponentNotGiven());
								
								registeredUsers.getUser(usr).updateScore((2)*match.getSecondOpponentCorrect() + (-1)* match.getSecondOpponentIncorrect());
							}
							
							Comunication.write(client,result);
							
							challengeableUsers.addChallenger(match.getFirstOpponent(), match.getFirstOpponentUDPPort());
							challengeableUsers.addChallenger(match.getSecondOpponent(), match.getSecondOpponentUDPPort());
							
							currentMatches.removeMatch(usr);
							currentMatches.removeMatch(message);
						}
						
						activeRequests.offer(client);
					break;
		        	
				default:
					break;
					        
				}
			}
			catch(SocketTimeoutException e1) {
				activeRequests.offer(client);
			}
			catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}
}