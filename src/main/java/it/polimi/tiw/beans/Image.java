package it.polimi.tiw.beans;

import java.util.Date;

public class Image {
	private final int id;
	private final String path, title, description;
	private final Date date;

	public Image(int id, String path, Date date, String title, String description) {
		this.id = id;
		this.path = path;
		this.date = date;
		this.title = title;
		this.description = description;
	}
	
	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getPath() {
		return path;
	}

	public int getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}
}
