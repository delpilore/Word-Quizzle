package source;

//AUTHOR: Lorenzo Del Prete, Corso B, 531417

/*
* STATUSCODES
* 
* Questa enumerazione definisce tutte le possibili risposte che il server pu� dare ad una richiesta da parte di un client.
* Al singolo caso � associata una stringa che vuole rappresentare una sorta di "reason phrase alla HTTP" utile per l'utente umano.
*/

public enum StatusCodes {
	USERNOTREGISTERED("Non sei registrato!\n"),
	WRONGPASSWORD("La password inserita non � corretta!\n"),
	OK("L'operazione � andata a buon fine!\n"),
	WRONGFRIENDREQUEST("L'utente specificato non � registrato a Word Quizzle!\n"),
	ALREADYFRIENDS("Sei gi� amico con questo utente!\n");

    public final String label;
    
    private StatusCodes(String label) {
        this.label = label;
    }
}