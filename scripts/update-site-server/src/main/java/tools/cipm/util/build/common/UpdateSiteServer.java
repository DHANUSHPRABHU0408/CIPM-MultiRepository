package tools.cipm.util.build.common;

import java.nio.file.Path;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.resource.Resource;

public class UpdateSiteServer {
    private Server server;
    private ContextHandlerCollection handler;

    public void start() throws Exception {
        if (this.server != null && this.server.isStarted()) {
            throw new IllegalStateException("Server was already started.");
        } else if (this.server != null) {
            server.start();
            return;
        }

        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost("localhost");
        connector.setPort(8081);
        server.addConnector(connector);

        handler = new ContextHandlerCollection();
        server.setHandler(handler);

        server.start();
    }

    public void addDirectoryWithStaticContent(String contextPath, Path directory) throws Exception {
        if (this.server == null) {
            throw new IllegalStateException("Cannot add a directory when the server is not started.");
        }

        ResourceHandler resource = new ResourceHandler();
        resource.setBaseResource(Resource.newResource(directory));
        resource.setDirectoriesListed(false);

        ContextHandler context = new ContextHandler(contextPath);
        context.setHandler(resource);
        handler.deployHandler(context, Callback.NOOP);
        context.start();
    }

    public void stop() throws Exception {
        if (this.server != null) {
            server.stop();
        }
    }
}
