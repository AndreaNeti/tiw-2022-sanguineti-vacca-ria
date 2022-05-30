package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

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

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;
import it.polimi.tiw.utils.Utils;

@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public Login() {
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

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// obtain and escape params
		String usrn = null;
		String pwd = null;

		Messages errorMsg = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));

		if (StringUtils.isBlank(usrn) || StringUtils.isBlank(pwd)) {
			errorMsg = Messages.EMPTY_CREDENTIALS;
		} else {
			usrn = StringUtils.strip(usrn);
		}
		if (errorMsg == null) {
			// query db to authenticate for user
			UserDAO userDao = new UserDAO(connection);
			User user = null;
			try {
				user = userDao.checkCredentials(usrn, pwd);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to check credentials");
				return;
			}

			// If the user exists, add info to the session and go to home page, otherwise
			// show login page with error message

			if (user == null) {
				errorMsg = Messages.WRONG_CREDENTIALS;
			} else {
				request.getSession().setAttribute("user", user);
				String path = getServletContext().getContextPath() + "/Home";
				response.sendRedirect(path);
				return;
			}
		}
		// there is some error
		String path = getServletContext().getContextPath() + "/Login";
		path = Utils.attachErrorToPath(path, errorMsg);
		response.sendRedirect(path);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		final WebContext ctx = new WebContext(request, response, getServletContext(), request.getLocale());

		String usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		if (!StringUtils.isBlank(usrn))
			ctx.setVariable("username", usrn);

		Utils.setMessages(request, ctx);
		templateEngine.process("WEB-INF/login.html", ctx, response.getWriter());
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