package it.polimi.tiw.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Album {
	private final int id;
	private final String title;
	private final Date date;

	public Album(int id, String title, Date date) {
		this.id = id;
		this.title = title;
		this.date = date;
	}

	public int getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		return sdf.format(date);
	}
}
