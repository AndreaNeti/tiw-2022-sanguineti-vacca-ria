package it.polimi.tiw.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;

public class ImageDAO {
	private Connection con;

	public ImageDAO(Connection connection) {
		this.con = connection;
	}

	public List<Image> getMyImages(User user) throws SQLException {
		List<Image> myImages = new ArrayList<Image>();
		String query = "SELECT  * FROM image  WHERE ID_User = ? ORDER BY Date DESC";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, user.getId());
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, getImage failed
					return Collections.emptyList();
				while (result.next()) {
					Image image = new Image(result.getInt("ID_Image"), result.getString("Path"),
							new Date(result.getDate("Date").getTime()), result.getString("Title"),
							result.getString("Description"));
					myImages.add(image);
				}
				return myImages;
			}
		}
	}

	public List<Image> getAlbumImages(int albumID) throws SQLException {
		List<Image> albumImages = new ArrayList<Image>();
		String query = "SELECT  I.ID_Image, I.Path, I.Date, I.Title, I.Description FROM image AS I JOIN image_album AS I_A ON I.ID_Image = I_A.ID_Image WHERE I_A.ID_Album = ? ORDER BY I.Date DESC";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, albumID);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, getImage failed
					return Collections.emptyList();
				while (result.next()) {
					Image image = new Image(result.getInt("ID_Image"), result.getString("Path"),
							new Date(result.getDate("Date").getTime()), result.getString("Title"),
							result.getString("Description"));
					albumImages.add(image);
				}
				return albumImages;
			}
		}
	}

	public Image getImage(int ID) throws SQLException {
		String query = "SELECT * FROM image WHERE ID_Image = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, ID);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, getImage failed
					return null;
				result.next();
				Image image = new Image(result.getInt("ID_Image"), result.getString("Path"),
						new Date(result.getDate("Date").getTime()), result.getString("Title"),
						result.getString("Description"));
				return image;
			}
		}
	}

	public void insertImage(User owner, String path, String title, String description) throws SQLException {
		String query;
		query = "INSERT INTO image (Path, Date, Title, Description, ID_User) VALUES (?, CURRENT_TIMESTAMP(), ?, ?, ?);";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, path);
			pstatement.setString(2, title);
			pstatement.setString(3, description);
			pstatement.setInt(4, owner.getId());
			pstatement.executeUpdate();
		}
	}

	public int getCurrentID() throws SQLException {
		int id;
		String query = "SELECT MAX(ID_Image) as last from image;";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			try (ResultSet result = pstatement.executeQuery();) {
				result.next();
				id = result.getInt("last");
				return id;
			}
		}
	}
}
