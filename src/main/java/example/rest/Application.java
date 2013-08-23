package example.rest;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Date: 8/23/13
 * Time: 12:11 PM
 */
public class Application extends ResourceConfig {
    public Application() {
        //packages("example.rest");
        register(SampleResource.class);
        //register(MultiPartFeature.class);
        register(JacksonFeature.class);
    }
}
