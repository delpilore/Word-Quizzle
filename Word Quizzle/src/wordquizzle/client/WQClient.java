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
		
		// Username e Password dell'utente che utilizzerà questo client
        String usr, pass;
		
        // Riferimento all'oggetto remoto esportato dal server per l'operazione di registrazione
		Remote remoteObject;
		RegisterInterface serverObject;
		
		// Messaggio di richiesta (vedere "Request") e messaggio di risposta (vedere "Response") per la comunicazione TCP tra client e server
		Request request = null;
		Response response = null;
		
		// Scanner per l'immissione dei comandi da parte dell'utente, con "command" la variabile che li conterrà
		Scanner input = new Scanner(System.in);
		String command;
		
		// Flags utili per uscire dai cicli principali di questo programma 
		// on si riferisce al programma client nella sua interezza (quando è impostato a false vuol dire che il programma si è chiuso completamente)
		// logged si riferisce al ciclo di un utente loggato, dove il programma continua ad attendere comandi da parte dell'utente
		Boolean on = true;
		Boolean logged = false;
		
		// La socket che verrà creata e connessa alla porta "serverTCPPort". Su di essa viaggeranno richieste e risposte in TCP.
		Socket socket = null;
		String hostname = "localhost"; 
		int serverTCPPort = 16000;
		
		// Porta su cui è localizzato il Registry del server, contenente i riferimenti agli oggetti remoti (in particolare quello per la registrazione)
		int serverRMIPort = 15000;
		
		// La porta UDP dove il thread Acceptor (istanziato poco sotto) ascolterà tutti i datagrammi UDP relativi all'operazione di Sfida
		int myUDPPort = ClientUtilities.portScanner(); 
		
		System.out.print("Benvenuto/a in Word Quizzle!\n");
		System.out.println("\tAvvio thread per accettazione richieste di sfida...");
		 
		// Thread che ascolterà tutti i datagrammi UDP relativi all'operazione di Sfida sulla porta rintracciata precedentemente da portScanner (vedere "ClientUtilities")
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
	        
	        // TRE POSSIBILI CASI: REGISTRAZIONE, LOGIN O CHIUDI
	        
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
		            
		            // Remote Method Invocation, registro l'utente con username <usr> e password <pass> attraverso l'oggetto remoto
		            // esportato dal server.
		            // Tutte le eccezioni catturabili sono elencate sotto; in caso il server sia spento
		            // al momento della richiesta, verrà stampato "Qualcosa è andato storto.."
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
	         * LOGIN (il caso più lungo, perché se andato a buon fine, gestisce tutte le richieste inviabili al server)
	         */
		        case "L":
		        case "l":
		        	System.out.print("\n\t-----------------\n");
		        	
		            System.out.print("\tNome utente: ");
		            usr = input.nextLine();
		            System.out.print("\tPassword: ");
		            pass = input.nextLine();
		            
		            // Istanzio una richiesta di LOGIN
		            request = new Request(usr, pass, Operations.LOGIN ,null);
		            
		        	try { 
		        		// Istanzio una socket connessa alla porta dove il server ascolta le richieste di conn. TCP (vedere "Listener" in wordquizzle.server)
		        		System.out.print("\tTentativo di connessione...\n");
		        		socket = new Socket(hostname, serverTCPPort);
		        		
		        		// Mando la richiesta e attendo una risposta
		        		Communication.write(socket, request);
		        		response = (Response) Communication.read(socket);
		        	}
		        	catch (Exception e) {
		        		// Nel caso il server sia spento o qualcosa non vada come previsto ritorno al menù principale con continue
		        		System.out.print("\tQualcosa è andato storto..\n");
		        		System.out.print("\t-----------------\n");
		        		continue;
			        }
		        	
		        	// Nel caso la connessione abbia avuto successo e il server mi abbia quindi inviato una risposta,
		        	// switcho a seconda dello statuscode contenuto in essa.
		        		
	        		switch(response.getStatusCode()) {
	        			
	        			case OK:
	        					
	        				System.out.print("\n\tSei loggato!\n");
	        				
	        				// Scrivo al server la porta UDP su cui il thread Acceptor ascolta 
	        				// (in modo che sappia a chi inoltrare le sfide quando qualcuno vorrà sfidarmi)
	        				Communication.write(socket, myUDPPort);
	        				
	        				System.out.print("\t-----------------\n");
  				
	        				// Ciclo del programma client loggato con un utente specifico
		        			logged=true;
		        			while(logged) {
		        				
		        				// Stampo le varie operazioni solo se il client non risulta in partita, per una questione di maggiore
		        				// leggibilità della CLI. (in partita do priorità ai messaggi relativi ad essa)
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
		        				
		        				// Switch tra tutte le possibili richieste del client
		        				switch (command) {
		        				
		        				
		        				
		        					//------------------------- LOGOUT -------------------------//
			        		        case "Lo":
			        		        case "lo":
			        		        	if (!Listener.isInChallenge() && !Listener.isChallenged()) {
				        		        	System.out.print("\n\t-----------------\n");
				        		        	
				        		        	System.out.print("\tEffettuo il logout\n");
				        		        	
				        		        	// Istanzio una richiesta di LOGOUT 
				        		        	// I campi null sono rispettivamente la password e l'eventuale dato aggiuntivo che il client deve comunicare al server (vedere "Request")
				        		            request = new Request(usr, null, Operations.LOGOUT, null);
				        		        	
				        		        	try {
				        		        		// Mando al server la richiesta e aspetto la risposta
				        		        		Communication.write(socket,request);
				        		        		response = (Response) Communication.read(socket);
			
				        		        		// Stampo lo statuscode e la reason phrase (vedere "StatusCodes")
				        		        		System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
				        		        		
				        		        		// Se lo statuscode è OK allora il logout è andato a buon fine e posso settare il flag logged a false.
				        		        		// In questo modo esco dal ciclo e ritorno a quello principale del client (registrazione, login e chiudi)
				        		        		if (response.getStatusCode() == StatusCodes.OK) 
				        		        			logged = false;		
				        		        	}
				        		        	catch(Exception e) {
				        		        		e.printStackTrace();
				        		        	}
				        		        	
				        		        	System.out.print("\t-----------------\n");
			        		        	}
			        		        	else {
			        		        		System.out.println("Non puoi utilizzare questo comando mentre sei in partita o hai una richiesta pendente!");
			        		        	}
			        		        break;
			        		        
			        		        
			        		        
			        		        //------------------------- AGGIUNGI AMICO -------------------------//
			        		        case "A":
			        		        case "a":
			        		        	if (!Listener.isInChallenge()) {
				        		        	System.out.print("\n\t-----------------\n");
				        		        	
				        		        	System.out.print("\tNome utente della persona che vuoi aggiungere come amico: ");
				        		        	String friend = input.nextLine();
				        		        	
				        		        	// Istanzio una richiesta di ADDFRIEND
				        		        	// In questo caso nell'ultimo campo inserisco il nome dell'utente che voglio aggiungere come amico
				        		        	request = new Request(usr, null, Operations.ADDFRIEND, friend);
				        		        
				        		        	try {
				        		        		// Mando la richiesta e attendo una risposta
				        		        		Communication.write(socket,request);
				        		        		response = (Response) Communication.read(socket);
				        		        		
				        		        		// Stampo lo statuscode e la reason phrase (vedere "StatusCodes")
				        		        		System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
				        		        		
				        		        	}
				        		        	catch(Exception e) {
				        		        		e.printStackTrace();
				        		        	}
				        		        	
				        		        	System.out.print("\t-----------------\n");
			        		        	}
			        		        	else {
			        		        		System.out.println("Non puoi utilizzare questo comando mentre sei in partita!");
			        		        	}
			        		        break;
			        		        
			        		        
			        		        
			        		        //------------------------- OTTIENI LISTA AMICI -------------------------//
			        		        case "La":
			        		        case "la":
			        		        	if (!Listener.isInChallenge()) {
				        		        	System.out.print("\n\t-----------------\n");
				        		        	
				        		        	// Istanzio una richiesta di FRIENDLIST
				        		        	request = new Request(usr, null, Operations.FRIENDLIST, null);
				        		        
				        		        	try {
				        		        		// Mando la richiesta ma questa volta non attendo una classica Response.
				        		        		// Attendo invece un oggetto JsonNode (libreria jackson): la lista delle amicizie di usr
				        		        		Communication.write(socket,request);
					        		            JsonNode json = (JsonNode) Communication.read(socket);
					        		            
					        		            if (json.isEmpty()){
					        		            	System.out.print("\tLa tua lista amici è vuota!\n");
					        		            }
					        		            else {
				        		        			System.out.print("\tEcco la tua lista amici:\n");
				        		        			// isArray() è un metodo messo a disposizione da jackson per "riconoscere" la struttura array in un oggetto Json
				        		        			if (json.isArray()) {
				        		        				// Infatti tratto l'oggetto come fosse un array, ciclando sopra di esso e stampando i valori contenuti
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
			        		        	}
			        		        	else {
			        		        		System.out.println("Non puoi utilizzare questo comando mentre sei in partita!");
			        		        	}
			        		        break;
			        		        
			        		        
			        		        
			        		        //------------------------- MOSTRA PUNTEGGIO -------------------------//
			        		        case "P":
			        		        case "p":	
			        		        	if (!Listener.isInChallenge()) {
				        		        	System.out.print("\n\t-----------------\n");
				        		        	
				        		        	// Istanzio una richiesta di SCORE
				        		        	request = new Request(usr, null, Operations.SCORE, null);
				        		        
				        		        	try {
				        		        		// Mando la richiesta e anche questa volta non attendo una classica Response.
				        		        		// Attendo invece un semplice int: il punteggio relativo all'utente usr
				        		        		Communication.write(socket,request);
				        		        		int score = (int) Communication.read(socket);
				        		        		
				        		        		System.out.print("\tIl tuo punteggio è: " + score + "\n");	
				        		        	}
				        		        	catch(Exception e) {
				        		        		e.printStackTrace();
				        		        	}
				        		        	
				        		        	System.out.print("\t-----------------\n");
			        		        	}
			        		        	else {
			        		        		System.out.println("Non puoi utilizzare questo comando mentre sei in partita!");
			        		        	}
			        		        break;
			        		        
			        		        
			        		        
			        		        //------------------------- MOSTRA CLASSIFICA -------------------------//
			        		        case "C":
			        		        case "c":
			        		        	if (!Listener.isInChallenge()) {
				        		        	System.out.print("\n\t-----------------\n");
				        		        	
				        		        	// Istanzio una richiesta di RANKING
				        		        	request = new Request(usr, null, Operations.RANKING, null);
				        		        
				        		        	try {
				        		        		// Mando la richiesta al server e, per l'ultima volta, non mi aspetto una classica Response.
				        		        		// Mi aspetto, come nel caso dell'operazione "OTTIENI LISTA AMICI", un oggetto JsonNode (jackson): la classifica già ordinata.
				        		        		Communication.write(socket,request);
					        		            JsonNode json = (JsonNode) Communication.read(socket);
					        		            
					        		            if (json.isEmpty()){
					        		            	System.out.print("\tLa tua lista amici è vuota!\n");
					        		            }
					        		            else {
					        		            	// L'oggetto Json sarà composto da diversi campi (nomi utente) e diversi valori associati (punteggio)
					        		            	// Queste coppie arrivano già ordinate in modo decrescente a seconda del punteggio.	
					        		            	// Itero su di esse e stampo, per ognuna, il campo seguito dal valore associato
					        		            	System.out.print("\tEcco la tua classifica con i tuoi amici:\n");
					        		            	Iterator<String> fieldNames = json.fieldNames();
	
					        		            	while(fieldNames.hasNext()) {
					        		            	    String fieldName = fieldNames.next();
					        		            	    // Qui recupero il valore associato ad un campo (si ricorda che il campo è il nome utente e il valore è il suo punteggio)
					        		            	    JsonNode field = json.get(fieldName);
					        		            	    // Stampo ogni coppia nella forma campo: valore
					        		            	    System.out.println("\t" + fieldName + ": " + field.asInt());
					        		            	}
					        		            }
				        		        	}
				        		        	catch(Exception e) {
				        		        		e.printStackTrace();
				        		        	}
				        		        	
				        		        	System.out.print("\t-----------------\n");
			        		        	}
			        		        	else {
			        		        		System.out.println("Non puoi utilizzare questo comando mentre sei in partita!");
			        		        	}
			        		        break;
			        		        
			        		        
			        		        
			        		        //------------------------- SFIDA UTENTE -------------------------//
			        		        case "S":
			        		        case "s":
			        		        	if (!Listener.isInChallenge()) {
				        		        	System.out.print("\n\t-----------------\n");
				        		        	
				        		        	System.out.print("\tNome utente dell'amico che vuoi sfidare: ");
				        		        	String opponent = input.nextLine();
				        		        	
				        		        	// Istanzio una richiesta CHALLENGEFRIEND 
				        		        	// L'ultimo campo della richiesta viene sfruttato per inviare al server il nome dello sfidante richiesto
				        		        	request = new Request(usr, null, Operations.CHALLENGEFRIEND, opponent);
				        		        
				        		        	try {
				        		        		// Mando la richiesta e attendo una risposta Response
				        		        		Communication.write(socket,request);
				        		        		response = (Response) Communication.read(socket);
			
				        		        		System.out.print("\t" + response.getStatusCode() + ": " + response.getStatusCode().label);
				        		        		
				        		        		// Se lo statuscode della Response equivale a MATCHSTARTING, la partita può ufficialmente iniziare.
				        		        		// Questo significa che lo sfidante ha accettato la richiesta di sfida inviata in TCP al server, che ha provveduto ad
				        		        		// inoltrarla in UDP al thread Acceptor dello sfidante. (che poi ha risposto "y", vedremo al case successivo cosa significa)
				        		        		if (response.getStatusCode()==StatusCodes.MATCHSTARTING) {
				        		        			// Setto il flag inChallenge dell'Acceptor di questo client a true, in modo che cambi il suo comportamento in fase di ascolto 
				        		        			// di datagrammi UDP: visto che siamo in partita, non dovrà più ascoltare richieste di sfida ma le parole da tradurre e infine
				        		        			// il risultato del match.
				        		        			Listener.setInChallenge(true);
				        		        			System.out.println("\tNel corso del match non potrai utilizzare nessun comando");
				        		        		}
				        		        	}
				        		        	catch(Exception e) {
				        		        		e.printStackTrace();
				        		        	}
				        		        	
				        		        	System.out.print("\t-----------------\n"); 	
			        		        	}
			        		        	else {
			        		        		System.out.println("Non puoi utilizzare questo comando mentre sei in partita!");
			        		        	}
			        		        break;
			        		        
			        		        // I prossimi due case sono attivi solo in particolari casi (altrimenti danno "Comando non riconosciuto")
			        		        // 	- "y"/"n" solo quando il client risulta sfidato da un altro utente (quindi è arrivata una richiesta di sfida a cui rispondere)
			        		        // 	- default normalmente manda "Comando non riconosciuto" ma in caso il client risulti in partita, manda ciò che è stato inserito in System.in
			        		        //	  tramite Scanner al server, come traduzione proposta della corrente parola del gioco.
			        		        
			        		        //------------------------- ACCETTA SFIDA / COMANDO NON RICONOSCIUTO -------------------------//
			        		        case "y":
			        		        case "n":
			        		        	// Funziona solo se il client risulta sfidato, quindi se il flag relativo contenuto nel Listener (thread Acceptor) è true
			        		        	if (Listener.isChallenged()) {
				        		        	try {
				        		        		// Che risponda "y" (si) o "n" (no) setto il flag challenged a false, visto che il client non risulta più sfidato.
				        		        		// O entra in partita o la rifiuta.
												Listener.setChallenged(false);
												// Stoppo il Timer che avevo attivato da Listener per attivarsi dopo 5 secondi a mancata risposta alla richiesta di sfida.
												// Se non avessi risposto in tempo "y" o "n" si sarebbe preso in carico lui di rispondere al server (in UDP) con "t" 
												// che quest'ultimo avrebbe poi girato come Response (StatusCodes.TIMEOUT), in TCP all'utente originario.
												Listener.stopTimer();
												
												// Se la risposta è "y" setto il flag inChallenge dell'Acceptor di questo client a true, in modo 
												// che cambi il suo comportamento in fase di ascolto di datagrammi UDP: visto che siamo in partita, 
												// non dovrà più ascoltare richieste di sfida ma le parole da tradurre e infineil risultato del match.
												if (command.equals("y")) {
													Listener.setInChallenge(true);
													System.out.print("\n\t-----------------\n");
													System.out.println("\tMATCHSTARTING: La partita sta per cominciare! Traduci più parole possibili in 60 secondi!");
													System.out.println("\tNel corso del match non potrai utilizzare nessun comando");
													System.out.print("\t-----------------\n"); 	
												}
												
												// In ogni caso mando la risposta alla richiesta ("y" o "n" che sia) al Server in UDP.
												// Che poi si occuperà di girarla all'utente originario (in TCP) e, nel caso, di gestire l'inizio della partita.
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
			        		        		// Nel caso il client non risulti "sfidato" il comando "y"/"n" è semplicemente non riconosciuto.
			        		        		System.out.println("\tComando non riconosciuto");
			        				break;
			        				
			        				
			        				
			        				//------------------------- MANDA PAROLA TRADOTTA / COMANDO NON RICONOSCIUTO -------------------------//
			        				default:
			        					// In caso il client risulti in partita ogni parola diversa dai casi precedenti ("lo", "a", "s", ecc...)
			        					// viene interpretata come traduzione proposta alla parola inviata dal server.
			        					if(Listener.isInChallenge()) {
			        						// Istanzio una richiesta utilizzando l'ultimo campo per contenere command, che in questo caso
			        						// contiene la traduzione proposta alla parola inviata dal server.
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
		        			
		        		// Questo default si riferisce alla possibile diversa risposta all'operazione di LOGIN.
		        		// Tutto il case precedente era in caso di risposta con StatusCode OK.
		        		// In caso, il login non abbia succeso viene stampato qui il motivo.
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
		        	// Interrompo il thread Acceptor (Listener) e setto il flag on a false, uscendo così dal 
		        	// ciclo principale del client e chiudendo così il programma nella sua interezza
		        	Acceptor.interrupt();
		        	on = false;
		        break;	
		        
	        /*
	         * COMANDO NON RICONOSCIUTO
	         */
		        default:
		        	System.out.println("Comando non riconosciuto");
		        break;
	        }
        }
        
        // Chiudo lo scanner per l'input 
        input.close();    
	}
}