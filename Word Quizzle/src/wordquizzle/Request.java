package wordquizzle;

import java.io.Serializable;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* REQUEST
* 
* Oggetto serializzabile che viene utilizzato dal client per inviare una richiesta al server.
* Contiene l'username, l'eventuale password, l'operazione richiesta (vedere "Operations") e 
* l'eventuale messaggio ("nome_utente" da aggiungere come amico, "nome_utente" da sfidare)
*/

public class Request implements Serializable {

	private static final long serialVersionUID = -8137609666799185094L;
	
	private String username;
	private String password;
	private Operations operation;
	private String message;
	
	public Request(String _username, String _password, Operations _operation, String _message) {
		username = _username;
		password = _password;
		operation = _operation;	
		message = _message;
	}
	
	// Metodi getters utili al server per estrarre le informazioni dal messaggio Request
	
	public String getRequestUsername() {
		return username;
	}
	
	public String getRequestPassword() {
		return password;
	}

	public Operations getRequestCommand() {
		return operation;
	}
	
	public String getRequestMessage() {
		return message;
	}
}