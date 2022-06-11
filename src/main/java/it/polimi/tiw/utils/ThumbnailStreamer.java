package it.polimi.tiw.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

@WebServlet("/ThumbnailStreamer")
@MultipartConfig
public class ThumbnailStreamer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	public static final String IMAGE_PATTERN = "^[0-9]{1,255}.(jpe?g|png)$";

	public ThumbnailStreamer() {
		super();
	}

	@Override
	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doGet(request, response);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String imageName = StringEscapeUtils.escapeJava(request.getParameter("image"));

		// checks if is a valid name file
		response.setContentType("image/jpeg");

		if (!imageName.matches(IMAGE_PATTERN)) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Illegal file access");
			return;
		}
		String path = Utils.getUploadDirectory(getServletContext()) + File.separator + imageName;

		File f = new File(path);
		BufferedImage source = ImageIO.read(f);
		int width = source.getWidth();
		int height = source.getHeight();
		int smallerSize = width > height ? height : width;
		BufferedImage croppedImage = crop(source, smallerSize);
		int type = croppedImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : croppedImage.getType();
		int size = 350;
		BufferedImage bi = resize(croppedImage, size, size, type);
		OutputStream out = response.getOutputStream();
		ImageIO.write(bi, imageName.split("\\.")[1], out);
		out.close();
	}

	private BufferedImage crop(BufferedImage source, int size) {
		BufferedImage img = source.getSubimage((source.getWidth() - size) / 2, (source.getHeight() - size) / 2, size,
				size);
		BufferedImage copyOfImage = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = copyOfImage.createGraphics();
		g.drawImage(img, 0, 0, null);
		return copyOfImage;
	}

	private BufferedImage resize(BufferedImage source, int width, int height, int type) {
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setComposite(AlphaComposite.Src);
		g.drawImage(source, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
