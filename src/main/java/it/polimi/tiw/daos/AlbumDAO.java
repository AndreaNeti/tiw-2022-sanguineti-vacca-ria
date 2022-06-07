package it.polimi.tiw.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;

public class AlbumDAO {
	private Connection con;

	public AlbumDAO(Connection connection) {
		this.con = connection;
	}

	public List<Album> getMyAlbums(User user) throws SQLException {
		List<Album> myAlbums = new ArrayList<Album>();
		String query = "SELECT  ID_Album, Title, Date, Value FROM album  WHERE ID_User = ? ORDER BY Value";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, user.getId());
			ResultSet result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) // no results, getAlbum failed or no albums present
				return Collections.emptyList();
			while (result.next()) {
				Album album = new Album(result.getInt("ID_Album"), result.getString("Title"),
						new Date(result.getDate("Date").getTime()), result.getInt("Value"));
				myAlbums.add(album);
			}
			return myAlbums;
		}
	}

	public List<Album> getOtherAlbums(User excludedUser) throws SQLException {
		List<Album> otherAlbums = new ArrayList<Album>();
		String query = "SELECT  ID_Album, Title, Date FROM album  WHERE ID_User <> ? ORDER BY Date DESC";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, excludedUser.getId());
			ResultSet result = pstatement.executeQuery();
			if (!result.isBeforeFirst()) // no results, getAlbum failed or no albums present
				return Collections.emptyList();
			while (result.next()) {
				Album album = new Album(result.getInt("ID_Album"), result.getString("Title"),
						new Date(result.getDate("Date").getTime()));
				otherAlbums.add(album);
			}
			return otherAlbums;
		}
	}

	/**
	 * 
	 * @param title
	 * @param owner
	 * @param imageID
	 * @throws SQLException
	 */
	public void newAlbum(String title, User owner, List<Integer> imageID) throws SQLException {
		con.setAutoCommit(false);
		int albumID;
		String query;
		query = "INSERT INTO album (Title, Date, ID_User) VALUES (?, CURRENT_TIMESTAMP(), ?);";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, title);
			pstatement.setInt(2, owner.getId());
			pstatement.executeUpdate();
		}
		// TODO get last id with sql function
		query = "SELECT MAX(ID_Album) as last from album;";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			ResultSet result = pstatement.executeQuery();
			result.next();
			albumID = result.getInt("last");
		}
		query = "INSERT INTO image_album (ID_Album, ID_Image) VALUES(?, ?);";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, albumID);
			for (Integer i : imageID) {
				pstatement.setInt(2, i);
				pstatement.addBatch();
			}
			pstatement.executeBatch();
		}
		query = "INSERT INTO album_order (ID_Album, ID_User) VALUES (?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, albumID);
			pstatement.setInt(2, owner.getId());
			pstatement.executeUpdate();
		} 
		con.commit();
	}
	
	public void changeOrder(User owner, List<Album> orderedList) throws SQLException {
		con.setAutoCommit(false);
		String query;
		query = "UPDATE album_order SET Value = ? WHERE ID_Album = ? AND ID_User = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(3, owner.getId());
			for (Album a : orderedList) {
				pstatement.setInt(1, a.getOrder());
				pstatement.setInt(2, a.getId());
				pstatement.addBatch();
			}
			pstatement.executeBatch();
		}
		con.commit();
	}
}
