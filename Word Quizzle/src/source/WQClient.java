package source;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import source.RegisterInterface.UserAlreadyRegisteredException;
import source.RegisterInterface.UsernameTooShortException;
import source.RegisterInterface.WeakPasswordException;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* WQCLIENT
* 
* WQClient è il programma client relativo al servizio Word Quizzle. 
* 
*/

public class WQClient {
	
	public static void main(String[] args) {
		
		Remote remoteObject;
		RegisterInterface serverObject;
		Request request = null;
		Response response = null;
		
		Scanner input = new Scanner(System.in);
		String command;
		Boolean on = true;
		Boolean logged = false;
		
		Socket socket = null;
		String hostname = "localhost"; 
		int myTCPPort = 16000;
		int myRMIPort = 15000;
		
        String usr, pass;
		
		System.out.print("Benvenuto/a in Word Quizzle!\n");
        
        while(on) {
        	
	   		System.out.print("\nusage : COMMAND [ ARGS ... ]\n"
	                 + "\tR: Registrati come nuovo utente\n"
	                 + "\tL: Effettua il login\n"
	                 + "\tX: Chiudi\n");
	        command = input.next();
	        
	        switch (command) {
	        
	        /*
	         * REGISTRAZIONE
	         */
		        case "R":
		        case "r":
		        	System.out.print("\n\t-----------------\n");
		            System.out.print("\tNome utente: ");
		            usr = input.next();
		            System.out.print("\tPassword: ");
		            pass = input.next();
		            
		    		try {
		    			Registry r = LocateRegistry.getRegistry(myRMIPort);
		    			remoteObject = r.lookup("REGISTER-SERVER");
		    			serverObject = (RegisterInterface) remoteObject;
		    			
		    			if (serverObject.registra_utente(usr, pass))
		    				System.out.print("\tRegistrato!\n");
		    			
		    		}
		    		catch(UserAlreadyRegisteredException e) {
		    			System.out.print("\tQuesto username è stato già preso!\n");
		    		}
		    		catch(WeakPasswordException e) {
		    			System.out.print("\tPassword troppo corta! Deve essere almeno di 4 caratteri!\n");
		    		}
		    		catch(UsernameTooShortException e) {
		    			System.out.print("\tNome utente troppo corto! Deve essere almeno di 3 caratteri!\n");
		    		}
		    		catch(Exception e) {
		    			System.out.print("\tQualcosa è andato storto..\n");
		    		}
		    		System.out.print("\t-----------------\n");
		            
				break;
				
	        /*
	         * LOGIN
	         */
		        case "L":
		        case "l":
		        	System.out.print("\n\t-----------------\n");
		            System.out.print("\tNome utente: ");
		            usr = input.next();
		            System.out.print("\tPassword: ");
		            pass = input.next();
		            
		            request = new Request(usr, pass, Operations.LOGIN ,null);
		            
		        	try { 
		        		System.out.print("\tTentativo di connessione...\n");
		        		socket = new Socket(hostname, myTCPPort);
		        		
		        		Utility.write(socket, request);
		        		response = (Response) Utility.read(socket);
		        	}
		        	catch (Exception e) {
			        	e.printStackTrace(); 
			        }
		        		
	        		switch(response.getStatusCode()) {
	        			
	        			case OK:
	        				System.out.print("\tSei loggato!\n");
	        				System.out.print("\t-----------------\n");
		        			System.out.print("\nPannello di controllo di " + usr + ", sei attualmente loggato a Word Quizzle!\n");
		        			logged=true;
		        			while(logged) {
		        				System.out.print( "\n\tLo: Effettua il logout\n"
		       		                 + "\tA: Aggiungi un amico\n"
		    		                 + "\tLa: Vedi la tua lista amici\n"
		    		                 + "\tS: Sfida un amico\n"
		    		                 + "\tP: Vedi il tuo punteggio\n"
		    		                 + "\tC: Vedi la classifica con i tuoi amici\n"
		    		         		 + "\tX: Chiudi (ed effettua il logout) \n\t" );
		        				command = input.next();
		        				
		        				switch (command) {
		        				
			        		        case "Lo":
			        		        case "lo":
			        		        case "X":
			        		        case "x":
			        		        	System.out.print("\n\t-----------------\n");
			        		        	
			        		        	if(command.equals("x") || command.equals("X")) {
				        		        	System.out.print("\tChiudo ed effettuo il logout\n");
				        		        	logged = false;
				        		        	on = false;
			        		        	}
			        		        	else 
			        		        		System.out.print("\tEffettuo il logout\n");
			        		        	
			        		            request = new Request(usr, null, Operations.LOGOUT, null);
			        		        	
			        		        	try {
	
				        		            Utility.write(socket,request);
			        		        		response = (Response) Utility.read(socket);
		
			        		        		System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
			        		        		if (response.getStatusCode() == StatusCodes.OK)
			        		        			logged = false;		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        	
			        		        	System.out.print("\t-----------------\n");
			        		        break;
			        		        
			        		        case "A":
			        		        case "a":
			        		        	
			        		        	System.out.print("\n\t-----------------\n");
			        		        	
			        		        	System.out.print("\tNome utente della persona che vuoi aggiungere come amico: ");
			        		        	String friend = input.next();
			        		        	request = new Request(usr, null, Operations.ADDFRIEND, friend);
			        		        
			        		        	try {
			        		        		
				        		            Utility.write(socket,request);
			        		        		response = (Response) Utility.read(socket);
		
			        		        		System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
			        		        		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        	
			        		        	System.out.print("\t-----------------\n");
			        		        break;
			        		        
			        		        case "La":
			        		        case "la":
			        		        	
			        		        	System.out.print("\n\t-----------------\n");
			        		        	
			        		        	request = new Request(usr, null, Operations.FRIENDLIST, null);
			        		        
			        		        	try {
			        		        		
				        		            Utility.write(socket,request);
				        		            
				        		            JsonNode json = (JsonNode) Utility.read(socket);
				        		            
				        		            if (json.isEmpty()){
				        		            	System.out.print("\tLa tua lista amici è vuota!\n");
				        		            }
				        		            else {
			        		        			System.out.print("\tEcco la tua lista amici:\n");
			        		        			if (json.isArray()) {
			        		        			    for (final JsonNode objNode : json) {
			        		        			        System.out.println("\t- " + objNode);
			        		        			    }
			        		        			}
				        		            }
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        	
			        		        	System.out.print("\t-----------------\n");
			        		        break;
			        		        
			        		        case "P":
			        		        case "p":
			        		        	
			        		        	System.out.print("\n\t-----------------\n");
			        		        	
			        		        	request = new Request(usr, null, Operations.SCORE, null);
			        		        
			        		        	try {
			        		        		
				        		            Utility.write(socket,request);
			        		        		int score = (int) Utility.read(socket);
			        		        		
			        		        		System.out.print("\tIl tuo punteggio è: " + score + "\n");
			        		        		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        	
			        		        	System.out.print("\t-----------------\n");
			        		        	
			        		        	
			        		        break;
			        		        
			        		        case "C":
			        		        case "c":
			        		        	
			        		        	System.out.print("\n\t-----------------\n");
			        		        	
			        		        	request = new Request(usr, null, Operations.RANKING, null);
			        		        
			        		        	try {
			        		        		
				        		            Utility.write(socket,request);
				        		            
				        		            JsonNode json = (JsonNode) Utility.read(socket);
				        		            
				        		            if (json.isEmpty()){
				        		            	System.out.print("\tLa tua lista amici è vuota!\n");
				        		            }
				        		            else {
				        		            	System.out.print("\tEcco la tua classifica con i tuoi amici:\n");
				        		            	Iterator<String> fieldNames = json.fieldNames();

				        		            	while(fieldNames.hasNext()) {
				        		            	    String fieldName = fieldNames.next();
				        		            	    JsonNode field = json.get(fieldName);
				        		            	    System.out.println("\t" + fieldName + ": " + field.asInt());
				        		            	}
				        		            }
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        	
			        		        	System.out.print("\t-----------------\n");
			        		        break;
		        				}
		        			}
		        		break;
		        			
	        			default:
	        				System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
	        				System.out.print("\t-----------------\n");
		        		break;
	        		}
		        	
		        break;
		        
	        /*
	         * CHIUDI
	         */
		        case "X":
		        case "x":
		        	System.out.print("Chiudo\n");
		        	on = false;
		        break;	
	        }
        }
        input.close();    
	}
}