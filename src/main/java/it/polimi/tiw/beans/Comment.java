package it.polimi.tiw.beans;

import java.util.Date;

public class Comment {
	private final String comment, nickname;
	private final Date date;

	public Comment(String comment, Date date, String nickname) {
		this.comment = comment;
		this.date = date;
		this.nickname = nickname;
	}

	public Date getDate() {
		return date;
	}

	public String getComment() {
		return comment;
	}
	
	public String getNickname() {
		return nickname;
	}
}
