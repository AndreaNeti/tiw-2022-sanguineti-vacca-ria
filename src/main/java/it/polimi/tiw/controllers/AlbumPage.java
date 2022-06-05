package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.Image;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;

@WebServlet("/AlbumPage")
@MultipartConfig
public class AlbumPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public AlbumPage() {
		super();
	}

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Messages errorMsg = null;
		String albumIdString = StringEscapeUtils.escapeJava(request.getParameter("album"));
		if (!StringUtils.isNumeric(albumIdString)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.INVALID_ALBUM.toString());
			return;
		}
		int albumId = Integer.parseInt(albumIdString);

		ImageDAO imageDao = new ImageDAO(connection);
		List<Image> images;
		try {
			images = imageDao.getAlbumImages(albumId);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		if (images.size() == 0)
			errorMsg = Messages.INVALID_ALBUM;

		if (errorMsg != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(errorMsg.toString());
			return;
		}
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
		String json = gson.toJson(images);
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
