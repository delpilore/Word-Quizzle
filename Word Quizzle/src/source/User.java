package source;

import java.util.ArrayList;

public class User {
	
	private String username;
	private String password;
	private Boolean OnlineState=false;
	private ArrayList<String> FriendList;
	private int score;

	public User(String _username, String _password) {
		setUsername(_username);
		setPassword(_password);
		setFriendList(new ArrayList<String>());
		setScore(0);
	}
	
	public User() {
		setUsername(null);
		setPassword(null);
		setFriendList(null);
		setScore(0);
	}

	private void setUsername(String username) {
		this.username = username;
	}
	
	private void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public void setOnlineState(Boolean _value) { 
		this.OnlineState = _value;
	}
	
	public boolean getOnlineState() {
		return OnlineState;
	}

	public ArrayList<String> getFriendList() {
		return FriendList;
	}

	public void setFriendList(ArrayList<String> _friendList) {
		this.FriendList = _friendList;
	}
	
	public void addFriend(String _user) {
		FriendList.add(_user);
	}
	
	public boolean isFriend(String _user) {
		return FriendList.contains(_user);
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
	public void updateScore(int _matchscore) {
		score = score + _matchscore;
	}
}