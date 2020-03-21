package source;

import java.util.Hashtable;

public class Structures {

	private Hashtable<String, User> registered_users;  // Mappa il nome utente di un utente registrato al suo oggetto User, contentente altre informazioni (per ora solo password)
	
	public Structures() {
		registered_users = new Hashtable<String,User>();
	}
	
	public void reg_addUser(String _username, User _user) {
		registered_users.put(_username, _user);
	}
	
	public boolean reg_containsUser(String _username) {
		return registered_users.containsKey(_username);
	}
	
	public Hashtable<String, User> getRegistered(){
		return registered_users;
	}
	
}
