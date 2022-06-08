package it.polimi.tiw.controllers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import it.polimi.tiw.utils.Messages;

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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User me = (User) request.getSession().getAttribute("user");
		AlbumDAO albumDAO = new AlbumDAO(connection);
		String requestData = null;

		requestData = request.getReader().lines().collect(Collectors.joining());
		Type listType = new TypeToken<ArrayList<Album>>() {
		}.getType();
		List<Album> orderedAlbums = Collections.emptyList();
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
		try {
			orderedAlbums = gson.fromJson(requestData, listType);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.INVALID_ORDER.toString());
			return;
		}
		List<Album> myAlbums = null;
		try {
			myAlbums = albumDAO.getMyAlbums(me);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		// nlogn instead of n^2
		orderedAlbums.sort(Comparator.comparingInt(Album::getId));
		myAlbums.sort(Comparator.comparingInt(Album::getId));
		if (!myAlbums.equals(orderedAlbums)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.INVALID_ORDER.toString());
			return;
		}
		Set<Integer> values =  new HashSet<Integer>();
		for (Album a : orderedAlbums) {
			if (!values.add(a.getId()) || a.getOrder() < 1 || a.getOrder() > myAlbums.size()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(Messages.INVALID_ORDER.toString());
				return;
			}
		}
		try {
			albumDAO.changeOrder(me, orderedAlbums);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(Messages.ORDER_CHANGED.toString());
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
