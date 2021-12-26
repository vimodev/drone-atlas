package servlets;

import models.File;
import models.Image;
import models.Video;
import org.eclipse.jetty.util.ajax.JSON;
import org.json.simple.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ImageServlet extends HttpServlet {
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
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        File file = image.preloadFile();
        if (file == null) {
            response.getWriter().println(image.toJSON());
            return;
        }
        JSONObject result = image.toJSON();
        result.put("file", file.toJSON());
        response.getWriter().println(result);
    }
}