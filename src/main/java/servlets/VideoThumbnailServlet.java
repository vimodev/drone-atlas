package servlets;

import models.Video;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class VideoThumbnailServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String[]> parameters = request.getParameterMap();
        // Get the video id parameter
        if (parameters.get("id") == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String videoId = parameters.get("id")[0];
        // Try to find the video
        Video video = Video.find(videoId);
        if (video == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // Get the timestamp
        int time = 0;
        if (parameters.get("t") != null) {
            time = Integer.parseInt(parameters.get("t")[0]);
        }
        // Make the screenshot
        java.io.File screenshot = video.screenshot(time);
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
