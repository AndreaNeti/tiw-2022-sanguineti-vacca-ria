package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.AlbumDAO;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;

@WebServlet("/CreateAlbum")
@MultipartConfig
public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CreateAlbum() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User me = (User) request.getSession().getAttribute("user");
		List<Integer> selectedImageIds = new ArrayList<Integer>();
		String albumName = StringEscapeUtils.escapeJava(request.getParameter("AlbumTitle"));
		if (StringUtils.isBlank(albumName)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.EMPTY_ALBUMNAME.toString());
			return;
		}
		albumName = StringUtils.strip(albumName);
		if (!StringUtils.isAlphanumericSpace(albumName)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.ALPHANUMERIC_ALBUMNAME.toString());
			return;
		} else if (albumName.length() < 4) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.MIN_ALBUMNAME.toString());
			return;
		} else if (albumName.length() > 50) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.MAX_ALBUMNAME.toString());
			return;
		}
		String[] imageIds = request.getParameterValues("image");
		if (imageIds == null || imageIds.length == 0) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.EMPTY_ALBUM.toString());
			return;
		}
		// Selected images parse
		for (String s : imageIds) {
			if (!StringUtils.isNumeric(s)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(Messages.INVALID_ID.toString());
				return;
			}
			selectedImageIds.add(Integer.parseInt(StringEscapeUtils.escapeJava(s)));
		}
		AlbumDAO albumDao = new AlbumDAO(connection);
		try {
			albumDao.newAlbum(albumName, me, selectedImageIds);
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Impossible to query DB");
				return;
			}
			// SQLException --> foreign key constraint violation
			if (e.getErrorCode() == 1452) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(Messages.INVALID_ID.toString());
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Impossible to query DB");
			}
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(Messages.ALBUM_CREATED.toString());
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		User me = (User) request.getSession().getAttribute("user");
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> myImages;
		try {
			myImages = imageDAO.getMyImages(me);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
		String json = gson.toJson(myImages);
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
