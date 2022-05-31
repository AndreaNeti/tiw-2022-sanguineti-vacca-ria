package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;

@WebServlet("/Login")
@MultipartConfig
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Login() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
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
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to check credentials");
				return;
			}

			// If the user exists, add info to the session and go to home page, otherwise
			// show login page with error message

			if (user == null) {
				errorMsg = Messages.WRONG_CREDENTIALS;
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				request.getSession().setAttribute("user", user);

				response.setStatus(HttpServletResponse.SC_OK);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(user.getUsername());
				return;
			}
		}
		// there is some error
		response.getWriter().println(errorMsg.toString());
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