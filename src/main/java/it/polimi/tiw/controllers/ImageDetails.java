package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Comment;
import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.CommentDAO;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;
import it.polimi.tiw.utils.Utils;

@WebServlet("/ImageDetails")
public class ImageDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public ImageDetails() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		User me = Utils.checkUserSession(request, response);
		// not logged
		if (me == null)
			return;
		Messages errorMsg = null;
		Messages successMsg = null;
		String comment = StringEscapeUtils.escapeJava(request.getParameter("comment"));
		String albumIdString = StringEscapeUtils.escapeJava(request.getParameter("album"));
		String imageIdString = StringEscapeUtils.escapeJava(request.getParameter("image"));
		String pageIdString = StringEscapeUtils.escapeJava(request.getParameter("page"));
		if (!StringUtils.isNumeric(imageIdString) || !StringUtils.isNumeric(albumIdString)
				|| !StringUtils.isNumeric(pageIdString)) {
			errorMsg = Messages.INVALID_ID;
			String path = getServletContext().getContextPath() + "/Home";
			path = Utils.attachErrorToPath(path, errorMsg);
			response.sendRedirect(path);
			return;
		}
		int albumID = Integer.parseInt(albumIdString);
		int imageID = Integer.parseInt(imageIdString);
		int pageID = Integer.parseInt(pageIdString);
		if (StringUtils.isBlank(comment)) {
			errorMsg = Messages.EMPTY_COMMENT;
		} else {
			// Removing whitespace
			comment = StringUtils.strip(comment);
			CommentDAO commentDAO = new CommentDAO(connection);
			try {
				commentDAO.insertComment(me, imageID, comment);
			} catch (SQLException e) {
				if (e.getErrorCode() == 1452) {
					errorMsg = Messages.INVALID_ID;
					String path = getServletContext().getContextPath() + "/AlbumPage?album=" + albumID + "&page="
							+ pageID;
					path = Utils.attachErrorToPath(path, errorMsg);
					response.sendRedirect(path);
					return;
				}
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible to query DB");
				e.printStackTrace();
				return;

			}
			successMsg = Messages.COMMENT_INSERTED;
		}
		String path = getServletContext().getContextPath() + "/ImageDetails?album=" + albumID + "&page=" + pageID
				+ "&image=" + imageID;
		path = Utils.attachErrorToPath(path, errorMsg);
		path = Utils.attachSuccessToPath(path, successMsg);
		response.sendRedirect(path);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		User me = Utils.checkUserSession(request, response);
		// not logged
		if (me == null)
			return;
		String albumIdString = StringEscapeUtils.escapeJava(request.getParameter("album"));
		String imageIdString = StringEscapeUtils.escapeJava(request.getParameter("image"));
		String pageIdString = StringEscapeUtils.escapeJava(request.getParameter("page"));
		if (!StringUtils.isNumeric(imageIdString) || !StringUtils.isNumeric(albumIdString)
				|| !StringUtils.isNumeric(pageIdString)) {
			String path = getServletContext().getContextPath() + "/Home";
			path = Utils.attachErrorToPath(path, Messages.INVALID_ID);
			response.sendRedirect(path);
			return;
		}
		int albumID = Integer.parseInt(albumIdString);
		int imageID = Integer.parseInt(imageIdString);
		int pageID = Integer.parseInt(pageIdString);
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		try {
			// get image, if there is no image with that ID returns to album
			if (!update(albumID, imageID, pageID, ctx)) {
				// to avoid forward loop
				// forward back to album page
				String path = getServletContext().getContextPath() + "/AlbumPage?album=" + albumID + "&page=" + pageID;
				path = Utils.attachErrorToPath(path, Messages.INVALID_ID);
				response.sendRedirect(path);
				return;
			}
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible to query DB");
			e.printStackTrace();
			return;
		}
		Utils.setMessages(request, ctx);
		templateEngine.process("WEB-INF/imageDetails.html", ctx, response.getWriter());
	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private boolean update(int albumID, int imageID, int pageID, WebContext ctx) throws SQLException {
		ImageDAO imageDAO = new ImageDAO(connection);
		Image image;
		CommentDAO commentDAO = new CommentDAO(connection);
		List<Comment> comments;
		image = imageDAO.getImage(imageID);
		ctx.setVariable("album", albumID);
		ctx.setVariable("page", pageID);
		// There is no image with this ID
		if (image == null)
			return false;
		comments = commentDAO.getComments(image.getId());
		ctx.setVariable("image", image);
		ctx.setVariable("commentList", comments);
		return true;
	}
}
