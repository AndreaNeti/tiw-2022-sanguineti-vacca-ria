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
import org.apache.commons.validator.routines.EmailValidator;

import it.polimi.tiw.daos.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;

@WebServlet("/Register")
@MultipartConfig
public class Register extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Register() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Messages errorMsg = null;
		// obtain and escape params
		String usrn = null;
		String email = null;
		String pwd = null;
		String pwd2 = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		pwd = StringEscapeUtils.escapeJava(request.getParameter("pwd"));
		pwd2 = StringEscapeUtils.escapeJava(request.getParameter("pwd2"));

		if (StringUtils.isBlank(usrn) || StringUtils.isBlank(email) || StringUtils.isBlank(pwd)
				|| StringUtils.isBlank(pwd2)) {
			errorMsg = Messages.EMPTY_CREDENTIALS;
		} else {
			usrn = StringUtils.strip(usrn);
			email = StringUtils.strip(email);
			if (!StringUtils.isAlphanumericSpace(usrn))
				errorMsg = Messages.ALPHANUMERIC_USERNAME;
			else if (StringUtils.containsWhitespace(pwd))
				errorMsg = Messages.INVALID_PASSWORD_SPACES;
			else if (usrn.length() > 50)
				errorMsg = Messages.INVALID_MAX_USERNAME;
			else if (usrn.length() < 4)
				errorMsg = Messages.INVALID_MIN_USERNAME;
			else if (!EmailValidator.getInstance(false).isValid(email))
				errorMsg = Messages.INVALID_EMAIL;
			else if (pwd.length() < 4)
				errorMsg = Messages.INVALID_PASSWORD_LENGTH;
			else if (!pwd.equals(pwd2))
				errorMsg = Messages.PASSWORDS_NO_MATCH;
		}

		if (errorMsg == null) {
			UserDAO userDao = new UserDAO(connection);

			try {
				userDao.register(usrn, pwd, email);
			} catch (SQLException e) {
				// violated unique attribute
				if (e.getErrorCode() == 1062) {
					errorMsg = Messages.USERNAME_TAKEN;
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				} else {
					e.printStackTrace();
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Impossible to register");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Impossible to register");
				return;
			}
			// success
			if (errorMsg == null) {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println(usrn);
				return;
			}
		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
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
