package wordquizzle.server;

import java.util.Comparator;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* SCORECOMPARATOR
* 
* Comparatore utilizzato per riordinare una collezione di User a seconda del loro punteggio su Word Quizzle
* (utilizzato in "Utility")
*/

public class ScoreComparator implements Comparator<User> {
	
    public int compare(User a, User b) { 
    	return ((Integer)a.getScore()).compareTo(b.getScore());
    }
    
}