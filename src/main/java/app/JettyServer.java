package app;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import servlets.*;

import java.net.ContentHandler;

public class JettyServer {
    public static Server server;
    public static ServerConnector connector;

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
        connector = new ServerConnector(server);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});
        Handler apiHandler = createAPIHandler();
        Handler frontendHandler = createFrontendHandler();
        // Attach handlers to server
        HandlerCollection collection = new HandlerCollection();
        collection.setHandlers(new Handler[]{ apiHandler, frontendHandler});
        server.setHandler(collection);
        // Start the server
        server.start();
    }

    /**
     * Create the handler for the frontend static content
     * @return handler to handle frontend requests
     */
    private static Handler createFrontendHandler() {
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase(JettyServer.class.getClassLoader().getResource("frontend").toString());
        ContextHandler contextHandler = new ContextHandler("/");
        contextHandler.setHandler(resourceHandler);
        return contextHandler;
    }

    /**
     * Create the handler for the API requests
     * @return the servlet handler
     */
    private static Handler createAPIHandler() {
        // Specify servlet handlers
        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.setContextPath("/api");
        // File endpoints
        servletHandler.addServlet(FileServlet.class, "/file");
        servletHandler.addServlet(FileAllServlet.class, "/file/all");
        servletHandler.addServlet(FileOpenerServlet.class, "/file/open");
        // Video endpoints
        servletHandler.addServlet(VideoServlet.class, "/video");
        servletHandler.addServlet(VideoAllServlet.class, "/video/all");
        // Data point endpoints
        servletHandler.addServlet(DataPointServlet.class, "/point");
        servletHandler.addServlet(DataPointByVideoServlet.class, "/point/from");
        return servletHandler;
    }

    /**
     * Stop the server
     * @throws Exception if it fails
     */
    public static void stop() throws Exception {
        server.stop();
    }

}