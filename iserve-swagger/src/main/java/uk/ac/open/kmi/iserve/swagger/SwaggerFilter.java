package uk.ac.open.kmi.iserve.swagger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Luca Panziera on 10/09/2014.
 */
public class SwaggerFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(SwaggerFilter.class);

    private RequestDispatcher swaggerRequestDispatcher;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        if (req.getPathInfo().matches("/api-docs.*")) {
            logger.debug("Forwarding the request to Swagger");
            swaggerRequestDispatcher.forward(request, response);
        }
        chain.doFilter(request, response);

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.swaggerRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("Swagger");
    }
}