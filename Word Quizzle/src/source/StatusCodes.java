package source;

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
