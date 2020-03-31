package wordquizzle;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* STATUSCODES
* 
* Questa enumerazione definisce tutte le possibili risposte che il server pu� dare ad una richiesta da parte di un client.
* Ad ogni caso � associata una stringa che vuole rappresentare una sorta di "reason phrase alla HTTP" utile per l'utente umano.
*/

public enum StatusCodes {
	USERNOTREGISTERED("Non sei registrato!\n"),
	WRONGPASSWORD("La password inserita non � corretta!\n"),
	OK("L'operazione � andata a buon fine!\n"),
	WRONGREQUEST("L'utente specificato non � registrato a Word Quizzle!\n"),
	ALREADYFRIENDS("Sei gi� amico con questo utente!\n"),
	USERNOTONLINE("L'utente che vuoi sfidare non risulta online!\n"),
	USERINMATCH("L'utente che vuoi sfidare risulta gi� in una sfida!\n"),
	NOTFRIENDS("Non sei amico con questo utente!\n"),
	MATCHSTARTING("La partita sta per cominciare! Traduci pi� parole possibili in 60 secondi!\n"),
	MATCHDECLINED("La sfida � stata declinata!\n"),
	SELFREQUEST("Non puoi mandare una richiesta a te stesso!\n"),
	TIMEOUT("L'avversario non ha fatto in tempo ad accettare la tua richiesta di sfida!\n"),
	USERALREADYONLINE("Sei gi� loggato!\n");

    public final String label;
    
    private StatusCodes(String label) {
        this.label = label;
    }
}