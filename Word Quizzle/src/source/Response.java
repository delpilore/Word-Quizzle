package source;

import java.io.Serializable;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* RESPONSE
* 
* Oggetto serializzabile che viene utilizzato dal server come risposta ad una "Request" da parte di un client.
* Si limita a contenere uno StatusCodes (vedere file relativo "StatusCodes")
* In caso il server debba spedire altro, un oggetto Json per esempio, viene direttamente spedito con una write
* tutta sua. (e la response non viene inviata) (DA CAMBIARE)
*/

public class Response implements Serializable {

	private static final long serialVersionUID = 4780598293424730148L;
	
	private StatusCodes statusCode;
	
	public Response(StatusCodes _statusCode) {
		statusCode = _statusCode;
	}
	
	public StatusCodes getStatusCode() {
		return statusCode;
	}
	
}