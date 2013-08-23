package example.test;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Servlet;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * A utility class which allows us to access the application context hidden in
 * Jersey's test servlet container. All the JAX-RS resource tests should inherit
 * from this class.
 *
 * Date: 6/6/13
 * Time: 9:44 PM
 */
@Configuration
public class SpringContextAwareJerseyTest extends JerseyTest {
    protected static final String SERVLET_PATH = "/api";
    private static final String URL_MAPPING = SERVLET_PATH + "/*";

    final private static ThreadLocal<ApplicationContext> context =
        new ThreadLocal<>();


    @Override
    protected URI getBaseUri() {
        return UriBuilder.fromUri("http://localhost/").port(8080).build();
    }


    protected String getContextConfigLocation() {
        return getClass().getName();
    }

    /**
     * Return an application configuration. Since the real application will be
     * initialized later by our custom TestContainer, this one is just a dummy.
     *
     * @return Application an application configuration
     */
    @Override
    protected Application configure() {
        ResourceConfig config = new ResourceConfig();

        // return a dummy applicationContext.xml to make JerseyTest happy
        // otherwise it fails initialization claiming app context xml not found
        config.property("contextConfigLocation", "test-context.xml");
        return config.getApplication();
    }

    private Map<String, String> getInitParams() {
        Map<String, String> initParams = new HashMap<>();
        initParams.put("javax.ws.rs.Application",
                       "example.rest.Application");
        return initParams;
    }

    private Map<String, String> getContextParams() {
        String contextConfigLocation = getContextConfigLocation();
        Map<String, String> contextParams = new HashMap<>();
        contextParams.put("contextConfigLocation", contextConfigLocation);
        contextParams.put(
            "contextClass",
            "org.springframework.web.context.support.AnnotationConfigWebApplicationContext");
        return contextParams;
    }

    private List<String> getListeners() {
        List<String> listeners = new ArrayList<>();
        listeners.add("org.springframework.web.context.ContextLoaderListener");
        listeners.add("org.springframework.web.context.request.RequestContextListener");
        return listeners;
    }

    private List<String> getUrlMappings() {
        List<String> mappings = new ArrayList<>();
        mappings.add(URL_MAPPING);
        return mappings;
    }

    // Return our own TestContainerFactory
    private TestContainerFactory getTestContainerFactory(
        URI uri,
        Servlet servlet,
        Map<String, String> initParams,
        Map<String, String> contextParams,
        List<String> listeners,
        List<String> urlMappings) {
        return new SpringGrizzlyTestContainerFactory(uri,
                                                     servlet,
                                                     initParams,
                                                     contextParams,
                                                     listeners,
                                                     urlMappings);
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
        throws TestContainerException {
        return getTestContainerFactory(getBaseUri(),
                                       new ServletContainer(),
                                       getInitParams(),
                                       getContextParams(),
                                       getListeners(),
                                       getUrlMappings());
    }

    protected ApplicationContext getContext() {
        return context.get();
    }

    @Bean
    public static ContextHolder contextHolder() {
        return new ContextHolder();
    }

    private static class ContextHolder implements ApplicationContextAware {
        @Override
        public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
            context.set(applicationContext);
        }
    }
}
