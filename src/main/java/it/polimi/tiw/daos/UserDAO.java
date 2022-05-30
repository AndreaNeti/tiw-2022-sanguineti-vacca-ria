package it.polimi.tiw.daos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.codec.binary.Hex;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkCredentials(String usrn, String pwd) throws SQLException, NoSuchAlgorithmException {
		String query = "SELECT  ID_User, Username FROM user  WHERE Username = ? AND Password = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(pwd.getBytes());
			String hexString = Hex.encodeHexString(messageDigest.digest());
			pstatement.setString(2, hexString);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					return new User(result.getInt("ID_User"), result.getString("Username"));
				}
			}
		}
	}

	public void register(String username, String pwd, String email) throws SQLException, NoSuchAlgorithmException {
		String query = "INSERT INTO user (Username, Password, Email) VALUES (?, ?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(pwd.getBytes());
			String hexString = Hex.encodeHexString(messageDigest.digest());
			pstatement.setString(2, hexString);
			pstatement.setString(3, email);
			pstatement.executeUpdate();
		}
	}
}
