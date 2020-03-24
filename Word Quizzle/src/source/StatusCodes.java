package source;

public enum StatusCodes {
	USERNOTREGISTERED("Non sei registrato!\n"),
	WRONGPASSWORD("La password inserita non è corretta!\n"),
	OK("L'operazione è andata a buon fine!\n"),
	WRONGFRIENDREQUEST("L'utente specificato non è registrato a Word Quizzle!\n"),
	ALREADYFRIENDS("Sei già amico con questo utente!\n");
	
    public final String label;
    
    private StatusCodes(String label) {
        this.label = label;
    }
}
