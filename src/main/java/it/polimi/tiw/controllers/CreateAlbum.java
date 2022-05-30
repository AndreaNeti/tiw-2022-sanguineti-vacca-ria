package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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

import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.AlbumDAO;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;
import it.polimi.tiw.utils.Utils;

@WebServlet("/CreateAlbum")
public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CreateAlbum() {
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
		List<Integer> selectedImageIds = new ArrayList<Integer>();
		String albumName = StringEscapeUtils.escapeJava(request.getParameter("AlbumTitle"));
		if (StringUtils.isBlank(albumName))
			errorMsg = Messages.EMPTY_ALBUMNAME;
		else {
			albumName = StringUtils.strip(albumName);
			if (!StringUtils.isAlphanumericSpace(albumName))
				errorMsg = Messages.ALPHANUMERIC_ALBUMNAME;
			else if (albumName.length() < 4)
				errorMsg = Messages.MIN_ALBUMNAME;
			else if (albumName.length() > 50)
				errorMsg = Messages.MAX_ALBUMNAME;
		}
		String[] imageIds = request.getParameterValues("image");
		if (imageIds == null || imageIds.length == 0)
			errorMsg = Messages.EMPTY_ALBUM;

		if (errorMsg != null) {
			String path = getServletContext().getContextPath() + "/CreateAlbum";
			path = Utils.attachErrorToPath(path, errorMsg);
			response.sendRedirect(path);
			return;
		} else {
			for (String s : imageIds) {
				if (!StringUtils.isNumeric(s)) {
					errorMsg = Messages.INVALID_ID;
					break;
				}
				selectedImageIds.add(Integer.parseInt(StringEscapeUtils.escapeJava(s)));
			}
			if (errorMsg == null) {
				AlbumDAO albumDao = new AlbumDAO(connection);
				try {
					albumDao.newAlbum(albumName, me, selectedImageIds);
					successMsg = Messages.ALBUM_CREATED;
				} catch (SQLException e) {
					e.printStackTrace();
					try {
						connection.rollback();
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// SQLException --> foreign key constraint violation
					if (e.getErrorCode() == 1452) {
						errorMsg = Messages.INVALID_ID;
					} else {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Impossible to create album");
						return;
					}
				}
			}
			String path = getServletContext().getContextPath() + "/Home";
			path = Utils.attachErrorToPath(path, errorMsg);
			path = Utils.attachSuccessToPath(path, successMsg);
			response.sendRedirect(path);
		}

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		User me = Utils.checkUserSession(request, response);
		// not logged
		if (me == null)
			return;

		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		try {
			update(me, ctx);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible to query DB");
			e.printStackTrace();
			return;
		}
		Utils.setMessages(request, ctx);
		templateEngine.process("WEB-INF/createAlbum.html", ctx, response.getWriter());
	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void update(User me, WebContext ctx) throws SQLException {
		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> myImages;
		myImages = imageDAO.getMyImages(me);
		if (myImages.isEmpty())
			ctx.setVariable("errorMsg", Messages.EMPTY_IMAGES.toString());
		ctx.setVariable("myImages", myImages);
	}
}
