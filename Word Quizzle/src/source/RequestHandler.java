package source;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
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
						
						else if (!pass.equals(WordQuizzleUsers.getUser(usr).getPassword())) {
							response = new Response (StatusCodes.WRONGPASSWORD);
							Utility.write(client,response);
			        		client.close();
						}
						
						else {
							response = new Response (StatusCodes.OK);
							WordQuizzleUsers.getUser(usr).setOnlineState(true); 													
							Utility.write(client,response);
							
							// Se il login ha successo mi aspetto che il client mi invii la porta dove ascolta il suo
							// listener UDP
							int port = (int) Utility.read(client);
							WordQuizzleUsers.addChallenger(usr, port);
							
							synchronized(requestQueue) {
								requestQueue.add(client);
								requestQueue.notify();
							}
							
							WordQuizzleUsers.writeJson();
						}
					
		        	break;
		        	
					case LOGOUT:
	
						response = new Response (StatusCodes.OK);
						// Setto stato ad offline, di conseguenza non potrà più essere sfidato, quindi tolgo anche 
						// l'occorrenza dalla tabella in Structures
						WordQuizzleUsers.getUser(usr).setOnlineState(false);
						WordQuizzleUsers.removeChallenger(usr);
						Utility.write(client,response);
		        		
		        		client.close();
		        		
		        		WordQuizzleUsers.writeJson();
		        		
		        	break;
		        	
					case ADDFRIEND:
						
						if (!WordQuizzleUsers.containsUser(message)) {
							response = new Response (StatusCodes.WRONGFRIENDREQUEST);
							Utility.write(client,response);
						}
						else if (WordQuizzleUsers.getUser(usr).isFriend(message)){
							response = new Response (StatusCodes.ALREADYFRIENDS);
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
						
						String inp = "Sfida";
						
						byte buf[] = inp.getBytes();
						InetAddress ip = InetAddress.getLocalHost(); 
						DatagramSocket ds = new DatagramSocket();
						DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, WordQuizzleUsers.getChallenger(message)); 

				        ds.send(DpSend); 
					
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