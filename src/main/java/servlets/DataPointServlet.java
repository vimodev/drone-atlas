package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import models.DataPoint;
import models.File;
import models.Video;
import org.json.simple.JSONObject;

public class DataPointServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the id parameter
        String pointId = request.getParameter("id");
        if (pointId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // Try to find the point
        DataPoint point = DataPoint.find(pointId);
        if (point == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        // If we do not have to fetch relationships we are done
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        // Otherwise we recurse
        Video video = point.preloadVideo();
        if (video == null) {
            response.getWriter().println(point.toJSON());
            return;
        }
        File file = video.preloadFile();
        if (file == null) {
            JSONObject result = point.toJSON();
            result.put("video", video.toJSON());
            response.getWriter().println(result);
            return;
        }
        JSONObject vid = video.toJSON();
        vid.put("file", file.toJSON());
        JSONObject result = point.toJSON();
        result.put("video", vid);
        response.getWriter().println(result);
    }
}