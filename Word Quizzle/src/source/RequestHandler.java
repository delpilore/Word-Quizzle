package source;

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
							WordQuizzleUsers.getUser(usr).setOnlineState(true); // qua potrei anche aggiornare il file JSON ma mi sembra ragionevole che se il server si ferma mentre l'utente era online																// al riavvio risulti offline.
							Utility.write(client,response);
							
							synchronized(requestQueue) {
								requestQueue.add(client);
								requestQueue.notify();
							}
							
							WordQuizzleUsers.writeJson();
						}
					
		        	break;
		        	
					case LOGOUT:
	
						response = new Response (StatusCodes.OK);
						WordQuizzleUsers.getUser(usr).setOnlineState(false);
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