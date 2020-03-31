package wordquizzle.client;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Scanner;

import com.fasterxml.jackson.databind.JsonNode;

import wordquizzle.Operations;
import wordquizzle.RegisterInterface;
import wordquizzle.Request;
import wordquizzle.Response;
import wordquizzle.StatusCodes;
import wordquizzle.RegisterInterface.UserAlreadyRegisteredException;
import wordquizzle.RegisterInterface.UsernameTooLongException;
import wordquizzle.RegisterInterface.UsernameTooShortException;
import wordquizzle.RegisterInterface.WeakPasswordException;
import wordquizzle.Communication;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* WQCLIENT
* 
* WQClient è il programma client relativo al servizio Word Quizzle. 
*/

public class WQClient {
	
	public static void main(String[] args) {
		
        String usr, pass;
		
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
		int serverTCPPort = 16000;
		int serverRMIPort = 15000;
		int myUDPPort = ClientUtilities.portScanner(); // la porta UDP dove il thread per accettazione richieste di sfida ascolterà
		
		System.out.print("Benvenuto/a in Word Quizzle!\n");
		System.out.println("\tAvvio thread per accettazione richieste di sfida...");
		 
		// Thread per accettazione richieste di sfida
		ChallengeListener Listener = new ChallengeListener(myUDPPort);
		Thread Acceptor = new Thread(Listener);
		Acceptor.start();
		System.out.println("\tThread Attivato!");
		
        while(on) {
        	
	   		System.out.print("\nusage : Digita la lettera corrispondente al comando desiderato e premi invio!\n"
	                 + "\tR: Registrati come nuovo utente\n"
	                 + "\tL: Effettua il login\n"
	                 + "\tX: Chiudi\n");
	        command = input.nextLine();
	        
	        switch (command) {
	        
	        /*
	         * REGISTRAZIONE
	         */
		        case "R":
		        case "r":
		        	System.out.print("\n\t-----------------\n");
		            System.out.print("\tNome utente: ");
		            usr = input.nextLine();
		            System.out.print("\tPassword: ");
		            pass = input.nextLine();
		            
		    		try {
		    			Registry r = LocateRegistry.getRegistry(serverRMIPort);
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
		    		catch(UsernameTooLongException e) {
		    			System.out.print("\tNome utente troppo lungo! Deve essere massimo di 12 caratteri!\n");
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
		            usr = input.nextLine();
		            System.out.print("\tPassword: ");
		            pass = input.nextLine();
		            
		            request = new Request(usr, pass, Operations.LOGIN ,null);
		            
		        	try { 
		        		System.out.print("\tTentativo di connessione...\n");
		        		socket = new Socket(hostname, serverTCPPort);
		        		
		        		Communication.write(socket, request);
		        		response = (Response) Communication.read(socket);
		        	}
		        	catch (Exception e) {
			        	e.printStackTrace(); 
			        }
		        		
	        		switch(response.getStatusCode()) {
	        			
	        			case OK:
	        					
	        				System.out.print("\n\tSei loggato!\n");
	        				
	        				// Scrivo al server la porta UDP selezionata su cui il Listener per le sfide ascolterà
	        				Communication.write(socket, myUDPPort);
	        				
	        				System.out.print("\t-----------------\n");
  				
		        			logged=true;
		        			while(logged) {
		        				
		        				if(!Listener.isInChallenge()) {
				        			System.out.print("\nPannello di controllo di " + usr + ", sei attualmente loggato a Word Quizzle!\n");
			        				System.out.print( "\n\tLo: Effettua il logout (poi dal menù principale potrai chiudere il client)\n"
			       		                 + "\tA: Aggiungi un amico\n"
			    		                 + "\tLa: Vedi la tua lista amici\n"
			    		                 + "\tS: Sfida un amico\n"
			    		                 + "\tP: Vedi il tuo punteggio\n"
			    		                 + "\tC: Vedi la classifica con i tuoi amici\n");
		        				}
		        				command = input.nextLine();
		        				
		        				switch (command) {
		        				
			        		        case "Lo":
			        		        case "lo":
			        		        	System.out.print("\n\t-----------------\n");
			        		        	
			        		        	System.out.print("\tEffettuo il logout\n");
			        		        	
			        		            request = new Request(usr, null, Operations.LOGOUT, null);
			        		        	
			        		        	try {
	
			        		        		Communication.write(socket,request);
			        		        		response = (Response) Communication.read(socket);
		
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
			        		        	String friend = input.nextLine();
			        		        	request = new Request(usr, null, Operations.ADDFRIEND, friend);
			        		        
			        		        	try {
			        		        		
			        		        		Communication.write(socket,request);
			        		        		response = (Response) Communication.read(socket);
		
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
			        		        		
			        		        		Communication.write(socket,request);
				        		            
				        		            JsonNode json = (JsonNode) Communication.read(socket);
				        		            
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
			        		        		
			        		        		Communication.write(socket,request);
			        		        		int score = (int) Communication.read(socket);
			        		        		
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
			        		        		
			        		        		Communication.write(socket,request);
				        		            
				        		            JsonNode json = (JsonNode) Communication.read(socket);
				        		            
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
			        		        
			        		        case "S":
			        		        case "s":
			        		        	
			        		        	System.out.print("\n\t-----------------\n");
			        		        	System.out.print("\tNome utente dell'amico che vuoi sfidare: ");
			        		        	String opponent = input.nextLine();
			        		        	
			        		        	request = new Request(usr, null, Operations.CHALLENGEFRIEND, opponent);
			        		        
			        		        	try {
			        		        		
			        		        		Communication.write(socket,request);
			        		        		response = (Response) Communication.read(socket);
		
			        		        		System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
			        		        		if (response.getStatusCode()==StatusCodes.MATCHSTARTING) {
			        		        			Listener.setInChallenge(true);
			        		        			System.out.println("\tNel corso del match potrai continuare ad eseguire la maggior parte dei comandi");
			        		        		}
			        		        		
			        		        	}
			        		        	catch(Exception e) {
			        		        		e.printStackTrace();
			        		        	}
			        		        	
			        		        	System.out.print("\t-----------------\n");
				        		           
			        		        	
			        		        break;
			        		        
			        		        case "y":
			        		        case "n":
			        		        	if (Listener.isChallenged()) {
				        		        	try {
				        		        		
												Listener.setChallenged(false);
												Listener.stopTimer();
												
												// CORRETTO QUI
												if (command.equals("y")) {
													System.out.println("La partita sta per cominciare! Traduci più parole possibili in 60 secondi!");
													Listener.setInChallenge(true);
													System.out.println("Nel corso del match potrai continuare ad eseguire la maggior parte dei comandi");
												}
												
					        	            	InetAddress ip = InetAddress.getLocalHost(); 
					        		        
					        		    		byte[] send = command.getBytes("UTF8");
					        					DatagramPacket DpSend = new DatagramPacket(send, send.length, ip, Listener.getServerUDPPort()); 
					        					
												Listener.getDatagramSocket().send(DpSend);

				        		        	}
				        		        	catch(Exception e) {
				        		        		e.printStackTrace();
				        		        	}
			        		        	}
			        		        	else
			        		        		System.out.println("\tComando non riconosciuto");
			        				break;
			        				
			        				default:
			        					if(Listener.isInChallenge()) {
		        							request = new Request(usr, null, Operations.MATCH, command);
		        							Communication.write(socket, request);	
			        					}
			        					else {
			        						System.out.println("\tComando non riconosciuto");
			        					}
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
		        	Acceptor.interrupt();
		        	on = false;
		        break;	
		        
		        default:
		        	System.out.println("Comando non riconosciuto");
		        break;
	        }
        }
        input.close();    
	}
}