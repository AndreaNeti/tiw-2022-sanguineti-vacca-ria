package it.polimi.tiw.utils;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;

public class Utils {

	public static String getUploadDirectory(ServletContext context) {
		return context.getInitParameter("uploadDirectory");
	}

	public static User checkUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return null;
		}
		return (User) session.getAttribute("user");
	}
}
