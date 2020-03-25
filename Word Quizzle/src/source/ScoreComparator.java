package source;

import java.util.Comparator;

public class ScoreComparator implements Comparator<User> {
	
    public int compare(User a, User b) { 
    	return ((Integer)a.getScore()).compareTo(b.getScore());
    }
    
}