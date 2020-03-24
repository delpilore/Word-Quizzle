package source;

import java.io.Serializable;

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
