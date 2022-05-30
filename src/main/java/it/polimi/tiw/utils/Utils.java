package it.polimi.tiw.utils;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.thymeleaf.context.WebContext;

import it.polimi.tiw.beans.User;

public class Utils {

	public static String getUploadDirectory(ServletContext context) {
		return context.getInitParameter("uploadDirectory");
	}

	public static User checkUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			response.sendRedirect("Login");
			return null;
		}
		return (User) session.getAttribute("user");
	}

	public static void setMessages(HttpServletRequest request, WebContext ctx) {
		String success = StringEscapeUtils.escapeJava(request.getParameter("successMsg"));
		ctx.setVariable("successMsg", getMessage(success));

		String error = StringEscapeUtils.escapeJava(request.getParameter("errorMsg"));
		ctx.setVariable("errorMsg", getMessage(error));

	}

	private static String getMessage(String parameterMessage) {
		if (StringUtils.isNumeric(parameterMessage)) {
			Integer messageId = Integer.parseInt(parameterMessage);
			if (messageId < Messages.values().length && messageId >= 0) {
				return Messages.values()[messageId].toString();
			}
		}
		return null;
	}

	private static String attachMessageToPatch(String path, Messages message, boolean error) {
		if (message == null)
			return path;
		String s;
		if (!path.contains("?")) {
			s = path += "?";
		} else {
			s = path += "&";
		}
		if (error) {
			s += "errorMsg=";
		} else {
			s += "successMsg=";
		}
		return s + message.ordinal();
	}

	public static String attachErrorToPath(String path, Messages errorMsg) {
		return attachMessageToPatch(path, errorMsg, true);
	}

	public static String attachSuccessToPath(String path, Messages successMsg) {
		return attachMessageToPatch(path, successMsg, false);
	}
}
