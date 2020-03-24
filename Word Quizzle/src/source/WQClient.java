package source;

import java.net.Socket;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
		            System.out.print("Nome utente: ");
		            usr = input.next();
		            System.out.print("Password: ");
		            pass = input.next();
		            
		    		try {
		    			Registry r = LocateRegistry.getRegistry(myRMIPort);
		    			remoteObject = r.lookup("REGISTER-SERVER");
		    			serverObject = (RegisterInterface) remoteObject;
		    			
		    			if (serverObject.registra_utente(usr, pass))
		    				System.out.print("Registrato!\n");
		    			
		    		}
		    		catch(UserAlreadyRegisteredException e) {
		    			System.out.print("Questo username è stato già preso!\n");
		    		}
		    		catch(WeakPasswordException e) {
		    			System.out.print("Password troppo corta! Deve essere almeno di 4 caratteri!\n");
		    		}
		    		catch(UsernameTooShortException e) {
		    			System.out.print("Nome utente troppo corto! Deve essere almeno di 3 caratteri!\n");
		    		}
		    		catch(Exception e) {
		    			System.out.print("Qualcosa è andato storto..\n");
		    		}
		            
				break;
				
	        /*
	         * LOGIN
	         */
		        case "L":
		        case "l":
		            System.out.print("Nome utente: ");
		            usr = input.next();
		            System.out.print("Password: ");
		            pass = input.next();
		            
		            request = new Request(usr, pass, Operations.LOGIN ,null);
		            
		        	try { 
		        		System.out.print("Tentativo di connessione\n");
		        		socket = new Socket(hostname, myTCPPort);
		        		
		        		Utility.write(socket, request);
		        		response = (Response) Utility.read(socket);
		        	}
		        	catch (Exception e) {
			        	e.printStackTrace(); 
			        }
		        		
	        		switch(response.getStatusCode()) {
	        			
	        			case OK:
		        			System.out.print("\nPannello di controllo di " + usr + ", sei attualmente loggato a Word Quizzle!\n");
		        			logged=true;
		        			while(logged) {
		        				System.out.print( "\n\tLo: Effettua il logout\n"
		       		                 + "\tA: Aggiungi un amico\n"
		    		                 + "\tLa: Vedi la tua lista amici\n"
		    		                 + "\tS: Sfida un amico\n"
		    		                 + "\tMp: Vedi il tuo punteggio\n"
		    		                 + "\tMc: Vedi la classifica con i tuoi amici\n"
		    		         		 + "\tX: Chiudi (ed effettua il logout) \n\t" );
		        				command = input.next();
		        				
		        				switch (command) {
		        				
			        		        case "Lo":
			        		        case "lo":
			        		        case "X":
			        		        case "x":
			        		        	
			        		        	if(command.equals("x") || command.equals("X")) {
				        		        	System.out.print("Chiudo ed effettuo il logout\n");
				        		        	logged = false;
				        		        	on = false;
			        		        	}
			        		        	
			        		            request = new Request(usr, null, Operations.LOGOUT, null);
			        		        	
			        		        	try {
	
				        		            Utility.write(socket,request);
			        		        		response = (Response) Utility.read(socket);
		
			        		        		System.out.print(response.getStatusCode() + ": " + response.getStatusCode().label +"\n");
			        		        		if (response.getStatusCode() == StatusCodes.OK)
			        		        			logged = false;		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        break;
			        		        
			        		        case "A":
			        		        case "a":
			        		        	
			        		        	System.out.print("Nome utente della persona che vuoi aggiungere come amico: ");
			        		        	String friend = input.next();
			        		        	request = new Request(usr, null, Operations.ADDFRIEND, friend);
			        		        
			        		        	try {
			        		        		
				        		            Utility.write(socket,request);
			        		        		response = (Response) Utility.read(socket);
		
			        		        		System.out.print(response.getStatusCode() + ": " + response.getStatusCode().label +"\n");
			        		        		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        break;
			        		        
			        		        case "La":
			        		        case "la":
			        		        	
			        		        	request = new Request(usr, null, Operations.FRIENDLIST, null);
			        		        
			        		        	try {
			        		        		
				        		            Utility.write(socket,request);
				        		            
				        		            JsonNode json = (JsonNode) Utility.read(socket);

		        		        			System.out.print("Ecco la tua lista amici:\n");
		        		        			if (json.isArray()) {
		        		        			    for (final JsonNode objNode : json) {
		        		        			        System.out.println("\t- " + objNode);
		        		        			    }
		        		        			}
			        		        		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        break;
		        				}
		        			}
		        		break;
		        			
	        			default:
	        				System.out.print(response.getStatusCode() + ": " + response.getStatusCode().label +"\n");
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