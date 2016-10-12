package com.artbrains.test.rozhak.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;

import com.artbrains.test.rozhak.blurer.GaussianBlur;

/**
 * Servlet implementation class UploadFile
 */
@WebServlet(name = "UploadFile", urlPatterns = { "/UploadFile" })
public class UploadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;

	final String uploadPath = "C:/himanshu/uploads";
	// = getServletContext().getRealPath("/");

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		int blurRadius = Integer.parseInt(req.getParameter("blur"));
		String filename = req.getParameter("fileName");

		String filepath = uploadPath + "/" + filename;
		File file = new File(filepath);
		GaussianBlur.processImage(file, blurRadius);

		File downloadFile = new File(filepath);
		FileInputStream inStream = new FileInputStream(downloadFile);

		resp.setContentLength((int) downloadFile.length());

		String headerKey = "Content-Disposition";
		String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());
		resp.setHeader(headerKey, headerValue);
		OutputStream outStream = resp.getOutputStream();

		byte[] buffer = new byte[4096];
		int bytesRead = -1;

		while ((bytesRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, bytesRead);
		}

		inStream.close();
		outStream.close();
		downloadFile.delete();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		String fileName = "";
		if (isMultipart) {

			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			response.setContentType("text/plain");

			try {
				List<FileItem> items = upload.parseRequest(request);
				Iterator<FileItem> iterator = items.iterator();
				while (iterator.hasNext()) {
					FileItem item = (FileItem) iterator.next();
					if (!item.isFormField()) {
						fileName = item.getName();
						File path = new File(uploadPath);
						if (!path.exists()) {

							@SuppressWarnings("unused")
							boolean status = path.mkdirs();
						}

						File uploadedFile = new File(path + "/" + fileName);
						item.write(uploadedFile);
					}
				}
			} catch (FileUploadException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (fileName != null && !(fileName.isEmpty())) {
			response.getWriter().write(fileName);
		}

	}

	@Override
	public void destroy() {
		try {
			FileUtils.cleanDirectory(new File(uploadPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.destroy();
	}

}
