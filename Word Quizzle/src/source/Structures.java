package source;

import java.util.Hashtable;

public class Structures {

	private Hashtable<String, String> registered_users;
	
	public Structures() {
		registered_users = new Hashtable<String,String>();
	}
	
	public void reg_addUser(String username, String password) {
		registered_users.put(username,password);
	}
	
	public boolean reg_containsUser(String username) {
		return registered_users.containsKey(username);
	}
	
	public Hashtable<String, String> getRegistered(){
		return registered_users;
	}
	
}
