package uk.ac.open.kmi.iserve.elda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Luca Panziera on 17/09/2014.
 */
public class EldaFilter implements Filter {

    private Logger logger = LoggerFactory.getLogger(EldaFilter.class);
    private RequestDispatcher defaultRequestDispatcher;
    private RequestDispatcher restRequestDispatcher;
    private RequestDispatcher eldaRequestDispatcher;
    private RequestDispatcher swaggerRequestDispatcher;


    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // Obtain the actual request within the web application context
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        HttpServletResponse res = (HttpServletResponse) response;
        if (path.equals("") || path.equals("/")) {
            res.sendRedirect(httpRequest.getContextPath() + "/doc/services");
        } else if (path.equals("/api-docs")) {
            swaggerRequestDispatcher.forward(request, response);
        } else if (path.equals("/docs")) {
            res.sendRedirect(httpRequest.getContextPath() + "/docs/");
        } else if (path.equals("/docs/")) {
            request.getRequestDispatcher(path + "index.html").forward(request, response);
        } else if (path.matches("/docs/.*") || path.matches("/lda-assets.*")) {
            defaultRequestDispatcher.forward(request, response);
        } else if (path.matches("/doc/.*")) {
            logger.debug("Forward request to Elda...");
            eldaRequestDispatcher.forward(request, response);
        } else if ((path.matches("/id/.*") && !httpRequest.getMethod().equalsIgnoreCase("GET")) || path.matches("/id/documents.*")) {
            logger.debug("Forward request to iServe REST...");
            restRequestDispatcher.forward(request, response);
        } else if (path.matches("/id/.*") && httpRequest.getMethod().equalsIgnoreCase("GET")) {
            logger.debug("Redirect request to Elda...");
            res.sendRedirect(httpRequest.getContextPath() + path.replace("/id/", "/doc/"));
        } else if (path.matches("/kb.*")) {
            logger.debug("Forward request to iServe REST...");
            restRequestDispatcher.forward(request, response);
        } else if (path.matches("/discovery.*")) {
            logger.debug("Forward request to iServe REST...");
            restRequestDispatcher.forward(request, response);
        } else {
            logger.debug("Forward request to Filter chain...");
            chain.doFilter(request, response);
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.defaultRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("default");
        this.restRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("iServe REST Endpoint");
        this.swaggerRequestDispatcher = filterConfig.getServletContext().getNamedDispatcher("Swagger");
        this.eldaRequestDispatcher = filterConfig.getServletContext().getNamedDispatcher("Elda");

    }
}
