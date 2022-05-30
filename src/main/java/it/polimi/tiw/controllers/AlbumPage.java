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

import it.polimi.tiw.beans.Image;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;
import it.polimi.tiw.utils.Utils;

@WebServlet("/AlbumPage")
public class AlbumPage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public AlbumPage() {
		super();
	}

	@Override
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		User me = Utils.checkUserSession(request, response);
		// not logged
		if (me == null)
			return;
		Messages errorMsg = null;
		String albumIdString = StringEscapeUtils.escapeJava(request.getParameter("album"));
		String pageIdString = StringEscapeUtils.escapeJava(request.getParameter("page"));
		if (!StringUtils.isNumeric(albumIdString) || !StringUtils.isNumeric(pageIdString)) {
			errorMsg = Messages.INVALID_ALBUM;
		}
		int albumId = Integer.parseInt(albumIdString);
		int page = Integer.parseInt(pageIdString);

		ImageDAO imageDao = new ImageDAO(connection);
		List<Image> images;
		try {
			images = imageDao.getAlbumImages(albumId);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Impossible to query DB");
			e.printStackTrace();
			return;
		}
		if (images.size() == 0) {
			errorMsg = Messages.INVALID_ALBUM;
		} else if (page * 5 > images.size() || page < 0) {
			errorMsg = Messages.INVALID_ALBUMPAGE;
		}
		
		if (errorMsg != null) {
			String path = getServletContext().getContextPath() + "/Home";
			path = Utils.attachErrorToPath(path, errorMsg);
			response.sendRedirect(path);
			return;
		}

		int fromIndex = page * 5;
		int toIndex = Math.min(page * 5 + 5, images.size());
		boolean rightButton = toIndex < images.size();
		boolean leftButton = fromIndex > 0;
		images = images.subList(fromIndex, toIndex);

		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());

		ctx.setVariable("albumImages", images);
		ctx.setVariable("album", albumId);
		ctx.setVariable("page", page);
		ctx.setVariable("rightButton", rightButton);
		ctx.setVariable("leftButton", leftButton);
		Utils.setMessages(request, ctx);
		templateEngine.process("WEB-INF/albumPage.html", ctx, response.getWriter());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
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
