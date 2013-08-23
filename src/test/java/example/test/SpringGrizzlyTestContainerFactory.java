package example.test;

/**
 * A customized Grizzly test container factory which creates a web container
 * with the same parameters as in the web.xml. It allows us to work
 * around a few bugs (or gaps) in the current Jersey-Spring integration and
 * ensures our test environment is as close to the real world as possible.
 *
 * Date: 8/18/13
 * Time: 2:38 PM
 */
import org.glassfish.grizzly.servlet.ServletRegistration;
import org.glassfish.grizzly.servlet.WebappContext;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.Servlet;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ApplicationHandler;
import org.glassfish.jersey.test.spi.TestContainer;
import org.glassfish.jersey.test.spi.TestContainerException;

import org.glassfish.grizzly.http.server.HttpServer;

public class SpringGrizzlyTestContainerFactory implements TestContainerFactory {

    private SpringGrizzlyTestContainer testContainer;

    private static class SpringGrizzlyTestContainer implements TestContainer {
        private static final Logger LOGGER =
            Logger.getLogger(SpringGrizzlyTestContainer.class.getName());

        private final URI uri;
        private Servlet servlet;
        private Map<String, String> initParams;
        private Map<String, String> contextParams;
        private List<String> listeners;
        private List<String> urlMappings;

        private HttpServer server;

        private SpringGrizzlyTestContainer(URI uri,
                                           Servlet servlet,
                                           Map<String, String> initParams,
                                           Map<String, String> contextParams,
                                           List<String> listeners,
                                           List<String> urlMappings) {
            this.uri = uri;
            this.servlet = servlet;
            this.initParams = initParams;
            this.contextParams = contextParams;
            this.listeners = listeners;
            this.urlMappings = urlMappings;
        }

        @Override
        public ClientConfig getClientConfig() {
            return null;
        }

        @Override
        public URI getBaseUri() {
            return uri;
        }

        private static HttpServer create(URI u, Servlet servlet,
                                         Map<String, String> initParams,
                                         Map<String, String> contextParams,
                                         List<String> listeners,
                                         List<String> urlMappings)
                throws IOException {
            if (u == null) {
                throw new IllegalArgumentException("The URI must not be null");
            }

            String path = u.getPath();

            WebappContext context = new WebappContext("GrizzlyContext", path);

            for (String listener: listeners ) {
                context.addListener(listener);
            }

            ServletRegistration registration =
                context.addServlet(servlet.getClass().getName(), servlet);

            for (String mapping: urlMappings) {
                registration.addMapping(mapping);
            }

            if(contextParams != null) {
                for(Map.Entry<String, String> e : contextParams.entrySet()) {
                    context.setInitParameter(e.getKey(), e.getValue());
                }
            }

            if (initParams != null) {
                registration.setInitParameters(initParams);
            }

            HttpServer server = GrizzlyHttpServerFactory.createHttpServer(u);
            context.deploy(server);

            return server;
        }

        @Override
        public void start() {
            LOGGER.info("start test container...");

            try {
                this.server = create(uri, servlet, initParams,
                                     contextParams, listeners, urlMappings);
            } catch (Exception e) {
                throw new TestContainerException(e);
            }
        }

        @Override
        public void stop() {
            LOGGER.info("stop test container...");
            this.server.stop();
        }
    }

    public SpringGrizzlyTestContainerFactory(URI uri,
                                             Servlet servlet,
                                             Map<String, String> initParams,
                                             Map<String, String> contextParams,
                                             List<String> listeners,
                                             List<String> urlMappings) {

        testContainer = new SpringGrizzlyTestContainer(uri,
                                                       servlet,
                                                       initParams,
                                                       contextParams,
                                                       listeners,
                                                       urlMappings);
    }

    @Override
    public TestContainer create(URI uri, ApplicationHandler appHandler) {
        return testContainer;
    }
}
