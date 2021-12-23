package servlets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import models.DataPoint;
import models.File;
import org.json.simple.JSONArray;

public class DataPointByVideoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the videoId parameter
        String videoId = request.getParameter("videoId");
        if (videoId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // Retrieve all files
        List<DataPoint> points = DataPoint.findByVideo(videoId);
        if (points == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        // Convert to JSON
        JSONArray json = new JSONArray();
        for (DataPoint point : points) {
            json.add(point.toJSON());
        }
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(json);
    }
}