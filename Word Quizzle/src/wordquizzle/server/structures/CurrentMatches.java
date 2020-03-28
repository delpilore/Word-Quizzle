package wordquizzle.server.structures;

import java.util.Hashtable;

import wordquizzle.server.Match;

public class CurrentMatches {

	private Hashtable<String, Match> matches;
	
	public CurrentMatches() {
		matches = new Hashtable<String, Match>();
	}
	
	public void addMatch(String usr, Match match) {
		matches.put(usr,match);
	}
	
	public Match getMatch(String usr) {
		return matches.get(usr);
	}
	
	public void removeMatch(String _user) {
		matches.remove(_user);
	}
}