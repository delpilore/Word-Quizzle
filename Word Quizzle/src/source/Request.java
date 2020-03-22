package source;

import java.io.Serializable;

public class Request implements Serializable {

	private static final long serialVersionUID = -8137609666799185094L;
	
	private String username;
	private String password;
	private String command;
	
	public Request(String _username, String _password, String _command) {
		username = _username;
		password = _password;
		command = _command;	
	}
	
	public String getRequestUsername() {
		return username;
	}
	
	public String getRequestPassword() {
		return password;
	}
	
	public String getRequestCommand() {
		return command;
	}
}
