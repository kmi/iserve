/*
 * Created by IntelliJ IDEA.
 * User: cp3982
 * Date: 30/10/2013
 * Time: 13:50
 */
package uk.ac.open.kmi.iserve.discovery.engine.rest;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.servlet.ServletModule;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
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
import uk.ac.open.kmi.iserve.core.PluginModuleLoader;
import uk.ac.open.kmi.iserve.core.SystemConfiguration;
import uk.ac.open.kmi.iserve.discovery.api.MatcherPluginModule;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Ranker;
import uk.ac.open.kmi.iserve.discovery.api.ranking.ScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.Scorer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.BasicScoreComposer;
import uk.ac.open.kmi.iserve.discovery.api.ranking.impl.StandardRanker;
import uk.ac.open.kmi.iserve.discovery.freetextsearch.FreeTextSearchProvider;
import uk.ac.open.kmi.iserve.discovery.ranking.impl.CommunityVitalityScorer;
import uk.ac.open.kmi.iserve.discovery.ranking.impl.ProviderPopularityScorer;
import uk.ac.open.kmi.iserve.sal.manager.NfpManager;
import uk.ac.open.kmi.iserve.sal.manager.impl.NfpManagerSparql;
import uk.ac.open.kmi.iserve.sal.manager.impl.RegistryManagementModule;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DiscoveryRestModule extends ServletModule {

    private Logger logger = LoggerFactory.getLogger(DiscoveryRestModule.class);
    private String basePath;

    @Inject
    public DiscoveryRestModule(@Named(SystemConfiguration.ISERVE_URL_PROP) String iserveUrl) {
        basePath = iserveUrl;
    }

    @Override
    protected void configureServlets() {
        logger.debug("Loading Discovery Rest module...");

        logger.debug("Loading Discovery iServe components...");

        install(new RegistryManagementModule());
        // Load all matcher plugins
        install(PluginModuleLoader.of(MatcherPluginModule.class));

        // ServletModule defines the "request" scope. GuiceFilter creates/destroys the scope on each incoming request.
        install(new ServletModule());

        //Scorers configuration
        Multibinder<uk.ac.open.kmi.iserve.discovery.api.ranking.Filter> filterBinder = Multibinder.newSetBinder(binder(), uk.ac.open.kmi.iserve.discovery.api.ranking.Filter.class);

        //Scorers configuration
        Multibinder<Scorer> scorerBinder = Multibinder.newSetBinder(binder(), Scorer.class);
        scorerBinder.addBinding().to(CommunityVitalityScorer.class);
        scorerBinder.addBinding().to(ProviderPopularityScorer.class);
        bind(ScoreComposer.class).to(BasicScoreComposer.class);
        bind(Ranker.class).to(StandardRanker.class);
        bind(NfpManager.class).to(NfpManagerSparql.class);
        bind(DiscoveryResultsBuilderPlugin.class).to(DiscoveryResultsBuilder.class);

        //Free text search
        install(new FreeTextSearchProvider());

        bind(ServletContainer.class).in(Singleton.class);
        bind(ApiOriginFilter.class).in(Singleton.class);

        Map<String, String> props = new HashMap<String, String>();
        props.put("javax.ws.rs.Application", DiscoveryEngineWebApplication.class.getName());
        props.put("jersey.config.server.wadl.disableWadl", "true");
        serve("/*").with(ServletContainer.class, props);

        ReflectiveJaxrsScanner scanner = new ReflectiveJaxrsScanner();
        scanner.setResourcePackage(getClass().getPackage().getName());
        ScannerFactory.setScanner(scanner);
        SwaggerConfig config = ConfigFactory.config();
        config.setApiVersion("2.0");

        logger.debug("Setting swagger basePath: {}", basePath);
        config.setBasePath(basePath);
        ConfigFactory.setConfig(config);

        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());

        bootstrap();

        filter("/*", ApiOriginFilter.class.getName());

    }

    private void bootstrap() {
        FilterFactory.setFilter(new CustomFilter());
        logger.debug("Setting Swagger API info");
        ApiInfo info = new ApiInfo(
                "iServe Discovery RESTful APIs",                             /* title */
                "This is te documentation to the iServe functionalities for service discovery.  You can find out more about iServe" +
                        "at <a href=\"http://iserve.kmi.open.ac.uk\">http://iserve.kmi.open.ac.uk</a> or on irc.freenode.net, #swagger.  For this sample, " +
                        "you can use the api key \"special-key\" to test the authorization filters",
                "http://creativecommons.org/licenses/by-sa/3.0/",                  /* TOS URL */
                "iserve-general@googlegroups.com",                            /* Contact */
                "Apache 2.0",                                     /* license */
                "http://www.apache.org/licenses/LICENSE-2.0.html" /* license URL */
        );

        //  ConfigFactory.config().addAuthorization(oauth);
        ConfigFactory.config().setApiInfo(info);
    }

    public static class ApiOriginFilter implements javax.servlet.Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response,
                             FilterChain chain) throws IOException, ServletException {
            HttpServletResponse res = (HttpServletResponse) response;
            res.addHeader("Access-Control-Allow-Origin", "/*");
            res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
            res.addHeader("Access-Control-Allow-Headers", "Content-Type");
            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }
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
