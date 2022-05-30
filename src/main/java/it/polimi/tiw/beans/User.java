package it.polimi.tiw.beans;

public class User {
	private final int id;
	private final String username;

	public User(int id, String username) {
		this.id = id;
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public int getId() {
		return id;
	}
}
