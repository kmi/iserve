/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 30/10/2013
 * Time: 13:50
 */
package uk.ac.open.kmi.iserve.swagger;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.jaxrs.config.ReflectiveJaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.reader.ClassReaders;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.open.kmi.iserve.core.ConfigurationProperty;
import uk.ac.open.kmi.iserve.core.impl.iServePropertyImpl;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: This class should be revisited for adequate Guice integration
 */
public class SwaggerModule extends ServletModule {

    private Logger log = LoggerFactory.getLogger(SwaggerModule.class);
    private final Injector injector;

    @Inject
    public SwaggerModule(Injector registryInjector) {
        this.injector = registryInjector;
    }

    @Override
    protected void configureServlets() {
        log.debug("Loading Swagger module...");

//        install(new RegistryManagementModule());

        bind(ServletContainer.class).in(Singleton.class);
        bind(SwaggerFilter.class).in(Singleton.class);

        Map<String, String> props = new HashMap<String, String>();
        props.put("javax.ws.rs.Application", SwaggerWebApplication.class.getName());
        props.put("jersey.config.server.wadl.disableWadl", "true");
        serve("/*").with(ServletContainer.class, props);

        ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage("uk.ac.open.kmi.iserve");
        ScannerFactory.setScanner(scanner);
        SwaggerConfig config = ConfigFactory.config();
        config.setApiVersion("2.0");

        String basePath = injector.getInstance(Key.get(String.class,
                new iServePropertyImpl(ConfigurationProperty.ISERVE_URL)));

        log.debug("Setting swagger basePath: {}", basePath);
        config.setBasePath(basePath);
        ConfigFactory.setConfig(config);

        ClassReaders.setReader(new DefaultJaxrsApiReader());

        bootstrap();
    }

    private void bootstrap() {
        FilterFactory.setFilter(new CustomFilter());
        log.debug("Setting Swagger API info");
        ApiInfo info = new ApiInfo(
                "iServe RESTful API",                             /* title */
                "This is the documentation of the RESTful API to access iServe functionality. You can find out more about iServe " +
                        "at <a href=\"http://iserve.kmi.open.ac.uk\">http://iserve.kmi.open.ac.uk</a>.",
                "",                  /* TOS URL */
                "iserve-general@googlegroups.com",                            /* Contact */
                "Creative Commons Attribution-ShareAlike 3.0 Unported (CC BY-SA 3.0)",                                     /* license */
                "http://creativecommons.org/licenses/by-sa/3.0/" /* license URL */
        );

        //  ConfigFactory.config().addAuthorization(oauth);
        ConfigFactory.config().setApiInfo(info);
    }

    public class CustomFilter implements SwaggerSpecFilter {
        @Override
        public boolean isOperationAllowed(
                Operation operation,
                ApiDescription api,
                Map<String, java.util.List<String>> params,
                Map<String, String> cookies,
                Map<String, java.util.List<String>> headers) {
            return true;
        }

        @Override
        public boolean isParamAllowed(
                Parameter parameter,
                Operation operation,
                ApiDescription api,
                Map<String, java.util.List<String>> params,
                Map<String, String> cookies,
                Map<String, java.util.List<String>> headers) {
            return true;
        }
    }
}
