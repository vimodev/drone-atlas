import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JettyServer {
    private static Server server;

    private static int maxThreads = 100;
    private static int minThreads = 4;
    private static int idleTimeout = 120;

    /**
     * Start the Jetty webserver
     *
     * @throws Exception if something goes wrong
     */
    public static void start(int port) throws Exception {
        // Create the server with the specified thread configuration
        QueuedThreadPool threadPool = new QueuedThreadPool(maxThreads, minThreads, idleTimeout);
        server = new Server(threadPool);
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        // Specify handlers
        ServletHandler servletHandler = new ServletHandler();
        server.setHandler(servletHandler);
        servletHandler.addServletWithMapping(FileServlet.class, "/status");
        // Start the server
        server.start();
    }

    /**
     * Stop the server
     * @throws Exception if it fails
     */
    public static void stop() throws Exception {
        server.stop();
    }

}