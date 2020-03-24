package source;

public enum StatusCodes {
	USERNOTREGISTERED("Non sei registrato!\n"),
	WRONGPASSWORD("La password inserita non è corretta!\n"),
	OK("L'operazione è andata a buon fine!\n");
	
    public final String label;
    
    private StatusCodes(String label) {
        this.label = label;
    }
}
