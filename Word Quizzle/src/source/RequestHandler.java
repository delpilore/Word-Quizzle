package source;

import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

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
				//String message = request.getRequestMessage();
	
				switch (request.getRequestCommand()) {
					case LOGIN:
						
						if (!WordQuizzleUsers.containsUser(usr)) {
							response = new Response ("Non sei registrato!", StatusCodes.USERNOTREGISTERED);
							Utility.write(client,response);
			        		client.close();
						}
						
						else if (!pass.equals(WordQuizzleUsers.getUser(usr).getPassword())) {
							response = new Response ("Password sbagliata!", StatusCodes.WRONGPASSWORD);
							Utility.write(client,response);
			        		client.close();
						}
						
						else {
							response = new Response ("Da ora sei loggato!", StatusCodes.OK);
							WordQuizzleUsers.getUser(usr).setOnlineState(true); // qua potrei anche aggiornare il file JSON ma mi sembra ragionevole che se il server si ferma mentre l'utente era online																// al riavvio risulti offline.
							Utility.write(client,response);
							
							synchronized(requestQueue) {
								requestQueue.add(client);
								requestQueue.notify();
							}
						}
					
		        	break;
		        	
					case LOGOUT:
	
						response = new Response ("Da ora non sei più loggato!", StatusCodes.OK);
						WordQuizzleUsers.getUser(usr).setOnlineState(false);
						Utility.write(client,response);
		        		
		        		client.close();
		        		
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