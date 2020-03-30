package wordquizzle;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* OPERATIONS
* 
* Questa enumerazione definisce tutte le operazioni che possono essere richieste da un utente registrato, al Server.
*/

public enum Operations {
	LOGIN,
	LOGOUT,
	ADDFRIEND,
	FRIENDLIST,
	CHALLENGEFRIEND,
	SCORE,
	RANKING,
	MATCH
}