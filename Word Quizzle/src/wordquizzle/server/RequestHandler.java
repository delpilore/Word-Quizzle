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
import wordquizzle.Communication;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* REQUESTHANDLER
* 
* Generico task eseguito da un worker del server
*/

public class RequestHandler implements Runnable {
	
	// Socket TCP
	private Socket client;
	
	// Tutte le strutture condivise istanziate e passate da WQServer (vedere files relativi in wordquizzle.server.structures)
	private RegisteredUsers registeredUsers;
	private ChallengeableUsers challengeableUsers;
	private CurrentMatches currentMatches;
	private LinkedBlockingQueue<Socket> activeRequests;
	
	// Richiesta e risposta per comunicazione su socket TCP
	private Request request;
	private Response response;
	
	// Costruttore
	public RequestHandler(RegisteredUsers _registeredUsers, ChallengeableUsers _challengeableUsers, CurrentMatches _currentMatches, LinkedBlockingQueue<Socket> _activeRequests) {
		registeredUsers = _registeredUsers;
		challengeableUsers = _challengeableUsers;
		currentMatches = _currentMatches;
		activeRequests = _activeRequests;
	}
	
	// Metodo run()
	public void run() {
		
		while(true) {
			
			try {		
				// Estraggo una socket dalla LinkedBlockingQueue delle socket attive (inserite dal Listener o reinserite dagli stessi worker finchè l'utente associato non si slogga)
				// Tutte le socket hanno un timeout di 100msec impostato dal Listener, per non far bloccare i worker sulla read.
				client = activeRequests.take();
				
				// Mi blocco per 100msec sulla socket per leggere l'eventuale richiesta
				ObjectInputStream reader = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
				request = (Request) reader.readObject();

				// Recupero username, l'eventuale password e l'eventuale dato aggiuntivo
				String usr = request.getRequestUsername();
				String pass = request.getRequestPassword();
				String message = request.getRequestMessage();
	
				switch (request.getRequestCommand()) {
				
					case LOGIN:
						
						// Utente prova il login da non registrato
						if (!registeredUsers.isRegistered(usr)) {
							response = new Response (StatusCodes.USERNOTREGISTERED);
							Communication.write(client,response);
			        		client.close();
						}
						
						// Utente prova il login essendo già online da un altro client
						else if (registeredUsers.getUser(usr).getOnlineState()) {
							response = new Response (StatusCodes.USERALREADYONLINE);
							Communication.write(client,response);
			        		client.close();
						}
						
						// Utente prova il login inserendo una password errata
						else if (!pass.equals(registeredUsers.getUser(usr).getPassword())) {
							response = new Response (StatusCodes.WRONGPASSWORD);
							Communication.write(client,response);
			        		client.close();
						}
						
						else {
							// Se vengono passati tutti i controlli superiori mando al client una Response con StatusCode OK
							response = new Response (StatusCodes.OK);												
							Communication.write(client,response);
							
							// Se il login ha successo mi aspetto che il client mi invii anche la porta dove ascolta il suo
							// listener UDP
							int port = (int) Communication.read(client);
							
							// Aggiungo l'utente alla struttura challengeableUsers (perché è attualemente "sfidabile")
							// Setto il suo stato ad Online
							challengeableUsers.addChallenger(usr, port);
							registeredUsers.getUser(usr).setOnlineState(true);
							
							// Rimetto la socket nella LinkedBlockingQueue
							activeRequests.offer(client);
						}
					
		        	break;
		        	
					case LOGOUT:
						
						// Rimuovo l'utente da challengeableUsers e setto il suo stato ad offline
						challengeableUsers.removeChallenger(usr);
						registeredUsers.getUser(usr).setOnlineState(false);
						
						// Mando una Response con StatusCode OK e chiudo il socket senza rimetterlo in coda.
						response = new Response (StatusCodes.OK);
						Communication.write(client,response);
						client.close();
        		
		        	break;
		        	
					case ADDFRIEND:
						
						// Utente prova ad aggiungere un altro utente alla sua lista amici, ma quest'ultimo non risulta registrato
						if (!registeredUsers.isRegistered(message)) {
							response = new Response (StatusCodes.WRONGREQUEST);
							Communication.write(client,response);
						}
						
						// Utente prova ad aggiungere alla sua lista amici un utente con cui è già amico
						else if (registeredUsers.getUser(usr).isFriend(message)){
							response = new Response (StatusCodes.ALREADYFRIENDS);
							Communication.write(client,response);
						}
						
						// Utente prova a mandare una richiesta d'amicizia a se stesso
						else if (usr.equals(message)){
							response = new Response (StatusCodes.SELFREQUEST);
							Communication.write(client,response);
						}
						else {
							// Se tutti i controlli superiori passano mando una Response con StatusCode OK
							response = new Response (StatusCodes.OK);
							
							// Aggiungo la relazione d'amicizia ad entrambi gli utenti
							registeredUsers.getUser(usr).addFriend(message);
							registeredUsers.getUser(message).addFriend(usr);

							Communication.write(client,response);
							
							// Aggiorno lo stato del file Json
							registeredUsers.writeJson();
						}
						
						// Rimetto in coda nella LinkedBlockingQueue la socket del client in ogni caso
						activeRequests.offer(client);
						
		        	break;
		        	
					case FRIENDLIST:
						
						// Mando al client l'oggetto JsonNode (costruito attraverso metodi forniti da jackson)
						// che rappresenta le amicizie di usr
						ObjectMapper mapper = new ObjectMapper();
						JsonNode array = mapper.valueToTree(registeredUsers.getUser(usr).getFriendList());
						
						Communication.write(client,array);
						
						// Rimetto in coda nella LinkedBlockingQueue la socket del client
						activeRequests.offer(client);
						
		        	break;
		        	
					case SCORE:
						
						// Mando direttamente lo score dell'utente che l'ha richiesto
						Communication.write(client,registeredUsers.getUser(usr).getScore());
						
						// Rimetto in coda nella LinkedBlockingQueue la socket del client
						activeRequests.offer(client);
					
					break;
					
					case RANKING:
						
						// Costruisco l'oggetto JsonNode che rappresenta la classifica, attraverso il metodo getRanking 
						// (vedere "ServerUtilities") e lo spedisco al client
						JsonNode rankingTable = ServerUtilities.getRanking(registeredUsers, usr);
						
						Communication.write(client,rankingTable);
						
						// Rimetto in coda nella LinkedBlockingQueue la socket del client
						activeRequests.offer(client);
						
		        	break;
		        	
					case CHALLENGEFRIEND:
						
						// Utente tenta di sfidare un altro utente non registrato
						if (!registeredUsers.isRegistered(message)) {
							response = new Response (StatusCodes.WRONGREQUEST);
							Communication.write(client,response);
						}
						
						// Utente tenta di sfidare se stesso
						else if (usr.equals(message)){
							response = new Response (StatusCodes.SELFREQUEST);
							Communication.write(client,response);
						}
						
						// Utente tenta di sfidare un altro utente con cui non è amico
						else if (!registeredUsers.getUser(message).isFriend(usr)){
							response = new Response (StatusCodes.NOTFRIENDS);
							Communication.write(client,response);
						}	
						
						// Utente tenta di sfidare un altro utente che non risulta online
						else if (!registeredUsers.getUser(message).getOnlineState()) {
							response = new Response (StatusCodes.USERNOTONLINE);
							Communication.write(client,response);
						}
						
						// Utente tenta di sfidare un altro utente che risulta già in partita o ha già una richiesta pendente
						else if (!challengeableUsers.isChallengeable(message)) {
							response = new Response (StatusCodes.USERINMATCH);
							Communication.write(client,response);
						}
						else {
							// Se tutti i controlli sopra passano mi recupero le porte UDP dei listener corrispondenti a usr e message
							int usrPort = challengeableUsers.getChallengerPort(usr);
							int messagePort = challengeableUsers.getChallengerPort(message);
							
							// Tolgo entrambi gli utenti da challengeableUsers, sono infatti entrambi "bloccati" e non sfidabili.
							// Il server deve "capire" se la sfida avrà inizio oppure no, e ciò dipende dalla risposta alla richiesta di sfida di message.
							// Nel frattempo nessun'altro potrà invitare usr o message in partita.
							challengeableUsers.removeChallenger(usr);
							challengeableUsers.removeChallenger(message);
							
							// Recupero 5 parole italiane per la sfida con il metodo getWords() (vedere "ServerUtilities")
							ArrayList<String> selectedWords = ServerUtilities.getWords();
							
							byte buf[] = usr.getBytes("UTF8");
							byte[] receive = new byte[100]; 
							
							InetAddress ip = InetAddress.getLocalHost(); 
							DatagramSocket ds = new DatagramSocket();
							
							DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, messagePort); 
							DatagramPacket DpReceive = new DatagramPacket(receive, receive.length); 

							// Inoltro al client di message (attraverso la porta UDP recuperata inizialmente) la richiesta di sfida
							// E mi metto in attesa di una risposta (non sarà un'attesa infinita, perché se message non risponde entro 5 secondi
							// ci penserà un task a rispondere per lui)
							ds.send(DpSend); 
				            ds.receive(DpReceive);

				            // Se ricevo un "y" come risposta, la partita può avere inizio
				            if(GeneralUtilities.UDPToString(DpReceive).equals("y")) {
								response = new Response (StatusCodes.MATCHSTARTING);
								Communication.write(client,response);
								
								// Istanzio un oggetto Match che rappresenterà la partita
								Match match = new Match(usr, usrPort, message, messagePort, selectedWords);
								// Recupero le traduzioni delle parole italiane passate al costruttore 
								match.fetchTraductions();
								
								// Aggiungo un riferimento a questa partita sia per usr che per message
								// in modo da poter in entrambi i casi recuperare questo oggetto
								currentMatches.addMatch(usr, match);
								currentMatches.addMatch(message, match);
								
								// Istanzio un ReschedulableTimer (vedere file relativo) che prenderà un task MatchTimeOver (vedere file relativo)
								ReschedulableTimer timer = new ReschedulableTimer();
								MatchTimeOver task = new MatchTimeOver(currentMatches, match, challengeableUsers, registeredUsers);
								
								// Faccio partire la sfida mandando la prima parola ad entrambi gli sfidanti
								// e facendo partire il timer
								match.beginMatch();
								timer.schedule( task, 60000 );
								
								// Passo il timer all'oggetto Match precedentemente istanziato
								// Questo serve perché il seguente timer potrebbe aver bisogno di un rescheduling, in caso entrambi i giocatori finiscano prima
								// dei 60 secondi di gioco. (e di questo se ne occupano i metodi di Match)
								match.setTimer(timer);
				            }
				            // Se ricevo un "n" come risposta, la partita è stata declinata da message
				            else if (GeneralUtilities.UDPToString(DpReceive).equals("n")){
								response = new Response (StatusCodes.MATCHDECLINED);
								Communication.write(client,response);
								// Rimetto entrambi in challengeableUsers, visto che la sfida non deve iniziare, loro sono nuovamente "sfidabili"
								challengeableUsers.addChallenger(usr, usrPort);
								challengeableUsers.addChallenger(message, messagePort);
				            }
				            // Se ricevo un "t" come risposta, message non ha risposto in tempo alla richiesta di sfida
				            else {
				            	response = new Response (StatusCodes.TIMEOUT);
								Communication.write(client,response);
								// Rimetto entrambi in challengeableUsers, visto che la sfida non deve iniziare, loro sono nuovamente "sfidabili"
								challengeableUsers.addChallenger(usr, usrPort);
								challengeableUsers.addChallenger(message, messagePort);
				            }
				            
				            ds.close();
				            	
						}
						
						// Rimetto la socket del client nella LinkedBlockingQueue
						activeRequests.offer(client);
					
					break;
					
					case MATCH:
						
						// Mi arriva una Request con Operations MATCH, quindi una traduzione proposta (contenuta in message) ad una parola.
						// Recupero il Match dell'utente che me l'ha inviata e chiamo il metodo sendNextWord (vedere "Match")
						Match match = currentMatches.getMatch(usr);
						match.sendNextWord(usr,message);

						// Rimetto la socket del client nella LinkedBlockingQueue
						activeRequests.offer(client);
						
					break;
		        	
				default:
					break;
					        
				}
			}
			catch(SocketTimeoutException e1) {
				// Al termine dei 100msec di timeout, ogni socket viene semplicemente rimessa in coda nella LinkedBlockingQueue
				activeRequests.offer(client);
			}
			catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}
}