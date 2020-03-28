package wordquizzle.server.structures;

import java.util.Hashtable;

public class ChallengeableUsers {
	
	private Hashtable<String, Integer> challengers;
	
	public ChallengeableUsers() {
		challengers = new Hashtable<String, Integer>();
	}
	
	public void addChallenger(String _user, int _port) {
		challengers.put(_user,_port);
	}
	
	public int getChallengerPort(String _user) {
		return challengers.get(_user);
	}
	
	public void removeChallenger(String _user) {
		challengers.remove(_user);
	}
	
	public Boolean isChallengeable(String _user) {
		return challengers.containsKey(_user);
	}
}