package wordquizzle;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* STATUSCODES
* 
* Questa enumerazione definisce tutte le possibili risposte che il server può dare ad una richiesta da parte di un client.
* Ad ogni caso è associata una stringa che vuole rappresentare una sorta di "reason phrase alla HTTP" utile per l'utente umano.
*/

public enum StatusCodes {
	USERNOTREGISTERED("Non sei registrato!\n"),
	WRONGPASSWORD("La password inserita non è corretta!\n"),
	OK("L'operazione è andata a buon fine!\n"),
	WRONGREQUEST("L'utente specificato non è registrato a Word Quizzle!\n"),
	ALREADYFRIENDS("Sei già amico con questo utente!\n"),
	USERNOTONLINE("L'utente che vuoi sfidare non risulta online!\n"),
	USERINMATCH("L'utente che vuoi sfidare risulta già in una sfida!\n"),
	NOTFRIENDS("Non sei amico con questo utente!\n"),
	MATCHSTARTING("La partita sta per cominciare! Traduci più parole possibili in 60 secondi!\n"),
	MATCHDECLINED("La sfida è stata declinata!\n"),
	SELFREQUEST("Non puoi mandare una richiesta a te stesso!\n"),
	TIMEOUT("L'avversario non ha fatto in tempo ad accettare la tua richiesta di sfida!\n"),
	USERALREADYONLINE("Sei già loggato!\n");

    public final String label;
    
    private StatusCodes(String label) {
        this.label = label;
    }
}