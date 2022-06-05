package it.polimi.tiw.utils;

import javax.servlet.ServletContext;

public class Utils {

	public static String getUploadDirectory(ServletContext context) {
		return context.getInitParameter("uploadDirectory");
	}
}
