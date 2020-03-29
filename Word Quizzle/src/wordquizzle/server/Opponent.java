package wordquizzle.server;

public class Opponent {

	private String nick;
	private int UDPPort;
	private int currentWord;
	private int correctWords;
	private int incorrectWords;
	private int notGivenWords;
	private Boolean end;
	
	public Opponent(String _nick, int _port) {
		nick = _nick;
		UDPPort = _port;
		currentWord = 0;
		correctWords = 0;
		incorrectWords = 0;
		notGivenWords = 5;
		end = false;
	}

	public String getNick() {
		return nick;
	}

	public int getUDPPort() {
		return UDPPort;
	}

	public int getCurrentWord() {
		return currentWord;
	}

	public int getCorrectWords() {
		return correctWords;
	}

	public int getIncorrectWords() {
		return incorrectWords;
	}

	public int getNotGivenWords() {
		return notGivenWords;
	}

	public Boolean hasEnded() {
		return end;
	}
	
	public void updateCurrentWord() {
		currentWord++;
	}
	
	public void updateCorrectWords() {
		correctWords++;
	}
	
	public void updateIncorrectWords() {
		incorrectWords++;
	}
	
	public void updateNotGivenWords() {
		notGivenWords--;
	}
	
	public void end() {
		end=true;
	}
}