package it.polimi.tiw.controllers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.daos.ImageDAO;
import it.polimi.tiw.utils.ConnectionHandler;
import it.polimi.tiw.utils.Messages;
import it.polimi.tiw.utils.Utils;

@WebServlet("/UploadImage")
@MultipartConfig
public class UploadImage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final long MAX_FILE_SIZE = 10000000;
	private static final long MAX_REQUEST_SIZE = 10000000;
	private static final int MEMORY_THRESHOLD = 10000000;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public UploadImage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();

		connection = ConnectionHandler.getConnection(getServletContext());
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

		ImageDAO imageDao = new ImageDAO(connection);
		String imageTitle = null;
		String imageDescription = null;
		String filePath = null;
		int currentID = 0;

		if (ServletFileUpload.isMultipartContent(request)) {

			// Factory settings
			DiskFileItemFactory factory = new DiskFileItemFactory();
			factory.setSizeThreshold(MEMORY_THRESHOLD);
			factory.setRepository(new File(System.getProperty("java.io.tmpdir")));

			// Upload settings
			ServletFileUpload upload = new ServletFileUpload(factory);
			upload.setFileSizeMax(MAX_FILE_SIZE);
			upload.setSizeMax(MAX_REQUEST_SIZE);
			String uploadPath = Utils.getUploadDirectory(getServletContext());
			File uploadDir = new File(uploadPath);
			if (!uploadDir.exists()) {
				uploadDir.mkdir();
			}
			List<FileItem> formItems = upload.parseRequest(new ServletRequestContext(request));
			if (formItems != null && formItems.size() > 0) {
				String fileName = null;
				for (FileItem item : formItems) {
					if (!item.isFormField()) {
						try {
							currentID = imageDao.getCurrentID() + 1;
						} catch (SQLException e1) {
							e1.printStackTrace();
							response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server failure");
						}
						fileName = currentID + item.getName().substring(item.getName().lastIndexOf('.'));
						filePath = uploadPath + File.separator + fileName;
						File storeFile = new File(filePath);
						try {
							item.write(storeFile);
						} catch (Exception e) {
							e.printStackTrace();
							response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server failure");
						}
					} else {
						String s = item.getFieldName();
						if (s.equals("ImageTitle")) {
							imageTitle = item.getString();
						} else {
							imageDescription = item.getString();
						}
					}
				}
				try {
					imageDao.insertImage(me, fileName, imageTitle, imageDescription);
				} catch (SQLException e) {
					e.printStackTrace();
					response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed image upload");
					return;
				}
				Messages successMsg = Messages.IMAGE_UPLOADED;
				String path = getServletContext().getContextPath() + "/Home";
				//path = Utils.attachSuccessToPath(path, successMsg);
				response.sendRedirect(path);
				return;
			}
		}
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Error");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// If the user is not logged in (not present in session) redirect to the login
		User me = Utils.checkUserSession(request, response);
		// not logged
		if (me == null)
			return;

		String path = "/WEB-INF/uploadImage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		templateEngine.process(path, ctx, response.getWriter());
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
