package it.polimi.tiw.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.beans.User;

public class CommentDAO {
	private Connection con;

	public CommentDAO(Connection connection) {
		this.con = connection;
	}

	public List<Comment> getComments(int imageID) throws SQLException {
		String query = "SELECT C.Text, C.Date, U.Username FROM comments AS C JOIN user AS U ON C.ID_User = U.ID_User WHERE ID_Image = ?";
		List<Comment> comments = new ArrayList<Comment>();
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, imageID);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, getImage failed
					return Collections.emptyList();
				while (result.next()) {
					Comment comment = new Comment(result.getString("Text"), new Date(result.getDate("Date").getTime()),
							result.getString("Username"));
					comments.add(comment);
				}
				return comments;
			}
		}
	}

	public void insertComment(User user, int imageID, String text) throws SQLException {
		String query = "INSERT INTO comments (ID_Image, ID_User, Text, Date) VALUES (?, ?, ?, CURRENT_TIMESTAMP())";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setInt(1, imageID);
			pstatement.setInt(2, user.getId());
			pstatement.setString(3, text);
			pstatement.executeUpdate();
		}
	}
}
