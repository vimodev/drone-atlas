package servlets;

import models.Image;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageThumbnailServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the image id parameter
        String imageId = request.getParameter("id");
        if (imageId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // Try to find the image
        Image image = Image.find(imageId);
        if (image == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Make the screenshot
        java.io.File screenshot = image.thumbnail();
        // And send the file
        response.setContentType("image/jpeg");
        response.setStatus(HttpServletResponse.SC_OK);
        InputStream in = new FileInputStream(screenshot);
        OutputStream out = response.getOutputStream();
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
    }
}