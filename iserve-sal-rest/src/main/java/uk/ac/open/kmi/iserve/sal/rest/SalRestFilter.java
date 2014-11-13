package uk.ac.open.kmi.iserve.sal.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by Luca Panziera on 10/09/2014.
 */
public class SalRestFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(SalRestFilter.class);
    private RequestDispatcher defaultRequestDispatcher;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        logger.debug("Forwarding the request to SAL REST");
        defaultRequestDispatcher.forward(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.defaultRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("iServe SAL REST Endpoint");
    }
}