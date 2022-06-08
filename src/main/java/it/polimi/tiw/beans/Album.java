package it.polimi.tiw.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Album {
	private final int id;
	private final String title;
	private final Date date;
	private final int orderValue;

	public Album(int id, String title, Date date) {
		this.id = id;
		this.title = title;
		this.date = date;
		orderValue = 0;
	}

	public Album(int id, String title, Date date, int orderValue) {
		this.id = id;
		this.title = title;
		this.date = date;
		this.orderValue = orderValue;
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

	public int getOrder() {
		return orderValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Album a = (Album) o;
		return getId() == a.getId() && getTitle().equals(a.getTitle()) && getDate().equals(a.getDate());
	}
}
