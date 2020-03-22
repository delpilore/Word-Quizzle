package source;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RequestHandler implements Runnable {
	
	private Socket client;
	private Structures WordQuizzleUsers;
	
	private Request request;
	private Response response;
	
	public RequestHandler(Socket _client, Structures _support) {
		client = _client;
		WordQuizzleUsers = _support;
	}
	
	public void run() {
		
		try {
		
			ObjectOutputStream writer = new ObjectOutputStream(new BufferedOutputStream(client.getOutputStream()));
			ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
			
			request = (Request) reader.readObject();
			
			String usr = request.getRequestUsername();
			String pass = request.getRequestPassword();

			switch (request.getRequestCommand()) {
				case "L":
					
					if (!WordQuizzleUsers.containsUser(usr))
						response = new Response ("Non sei registrato!");
					
					else if (!pass.equals(WordQuizzleUsers.getUser(usr).getPassword()))
						response = new Response ("Password sbagliata!");
					
					else if (WordQuizzleUsers.getUser(usr).getOnlineState())
						response = new Response ("Sei già loggato!");	
					
					else {
						response = new Response ("Da ora sei loggato!");
						WordQuizzleUsers.getUser(usr).setOnlineState(true); // qua potrei anche aggiornare il file JSON ma mi sembra ragionevole che se il server si ferma mentre l'utente era online
																			// al riavvio risulti offline.
					}
					
	        		writer.writeObject(response);
	        		writer.flush();
	        		
	        		reader.close(); 
	        		writer.close(); 
	        		
	        	break;
	        	
				case "Lo":

					if (!WordQuizzleUsers.containsUser(usr))
						response = new Response ("Non sei registrato!");
					
					else if (!WordQuizzleUsers.getUser(usr).getOnlineState())
						response = new Response ("Non puoi effettuare il logout se prima non ti logghi!");	
					
					else {
						response = new Response ("Da ora non sei più loggato!");
						WordQuizzleUsers.getUser(usr).setOnlineState(false);
					}
					
	        		writer.writeObject(response);
	        		writer.flush();
	        		
	        		reader.close(); 
	        		writer.close(); 
				
	        	break;
					
	        	
	        	default:
	        		response = new Response ("comando non ancora gestibile");
	        		
	        		writer.writeObject(response);
	        		writer.flush();
	        		
	        		reader.close(); 
	        		writer.close(); 
	        		
	        	break;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
	}
}