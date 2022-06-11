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

import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.CommentDAO;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;

@WebServlet("/ImageDetails")
@MultipartConfig
public class ImageDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public ImageDetails() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		User me = (User) request.getSession().getAttribute("user");
		String comment = StringEscapeUtils.escapeHtml4(request.getParameter("comment"));
		String imageIdString = StringEscapeUtils.escapeJava(request.getParameter("image"));
		if (!StringUtils.isNumeric(imageIdString)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.INVALID_ID.toString());
			return;
		}
		int imageID = Integer.parseInt(imageIdString);
		if (StringUtils.isBlank(comment)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.EMPTY_COMMENT.toString());
			return;
		}
		// Removing whitespaces
		comment = StringUtils.strip(comment);
		CommentDAO commentDAO = new CommentDAO(connection);
		try {
			commentDAO.insertComment(me, imageID, comment);
		} catch (SQLException e) {
			// constraint fail, wrong id
			if (e.getErrorCode() == 1452) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(Messages.INVALID_ID.toString());
				return;
			}
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println(Messages.COMMENT_INSERTED.toString());
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String imageIdString = StringEscapeUtils.escapeJava(request.getParameter("image"));
		if (!StringUtils.isNumeric(imageIdString)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println(Messages.INVALID_ID.toString());
			return;
		}
		int imageID = Integer.parseInt(imageIdString);
		CommentDAO commentDAO = new CommentDAO(connection);

		List<Comment> comments;
		ImageDAO imageDAO = new ImageDAO(connection);
		try {
			// get image, if there is no image with that ID returns to album
			if (imageDAO.getImage(imageID) == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println(Messages.INVALID_ID.toString());
			}
			comments = commentDAO.getComments(imageID);
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Impossible to query DB");
			return;
		}
		Gson gson = new GsonBuilder().setDateFormat("dd/MM/yyyy").create();
		String json = gson.toJson(comments);
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
