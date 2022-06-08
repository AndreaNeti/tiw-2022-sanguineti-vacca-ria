package it.polimi.tiw.controllers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.beans.Album;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.AlbumDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetAlbums")
@MultipartConfig
public class GetAlbums extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetAlbums() {
		super();
	}

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		User me = (User) request.getSession().getAttribute("user");
		AlbumDAO albumDAO = new AlbumDAO(connection);
		String requestData = null;
		try {
			requestData = request.getReader().lines().collect(Collectors.joining());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Type listType = new TypeToken<ArrayList<Album>>(){}.getType();
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
		List<Album> orderedAlbums = gson.fromJson(requestData, listType);
		try {
			albumDAO.changeOrder(me, orderedAlbums);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User me = (User) request.getSession().getAttribute("user");
		AlbumDAO albumDao = new AlbumDAO(connection);
		AlbumsResponse albums = null;
		try {
			albums = new AlbumsResponse(albumDao.getMyAlbums(me), albumDao.getOtherAlbums(me));
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
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

	private record AlbumsResponse(List<Album> myAlbums, List<Album> otherAlbums) {
	};
}
