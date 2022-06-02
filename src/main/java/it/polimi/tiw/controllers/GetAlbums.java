package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.AlbumDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Utils;

@WebServlet("/GetAlbums")
@MultipartConfig
public class GetAlbums extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetAlbums() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		User me = Utils.checkUserSession(request, response);
		// not logged
		if (me == null)
			return;

		AlbumDAO albumDao = new AlbumDAO(connection);
		Map<Integer, List<Album>> albums = new HashMap<>();
		try {
			albums.put(0, albumDao.getMyAlbums(me));
			albums.put(1, albumDao.getOtherAlbums(me));
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		Gson gson = new GsonBuilder().setDateFormat("yyyy MM dd").create();
		String json = gson.toJson(albums);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
