package source;

public class User {
	
	private String username;
	private String password;

	public User(String _username, String _password) {
		setUsername(_username);
		setPassword(_password);
	}
	
	public User() {
		setUsername(null);
		setPassword(null);
	}

	private void setUsername(String username) {
		this.username = username;
	}
	
	private void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

}
