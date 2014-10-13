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

    private RequestDispatcher defaultRequestDispatcher;
    private RequestDispatcher salRestRequestDispatcher;
    private Logger logger = LoggerFactory.getLogger(EldaFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        res.addHeader("Access-Control-Allow-Origin", "*");
        res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        res.addHeader("Access-Control-Allow-Headers", "Content-Type");
        if (httpRequest.getRequestURI().equals("/iserve/id/services") && httpRequest.getMethod().equalsIgnoreCase("GET")) {
            // TODO Fix this by debugging Elda config file
            logger.debug("Redirection...");
            res.sendRedirect("/iserve/doc/services");
        } else if ((httpRequest.getRequestURI().matches("/iserve/id.*") && !httpRequest.getMethod().equalsIgnoreCase("GET")) || httpRequest.getRequestURI().matches("/iserve/id/documents.*")) {
            logger.debug("Forward to SAL REST");
            salRestRequestDispatcher.forward(request, response);
        } else if ((httpRequest.getRequestURI().matches("/iserve/id.*") && httpRequest.getMethod().equalsIgnoreCase("GET")) || (httpRequest.getRequestURI().matches("/iserve/api-docs.*"))) {
            logger.debug("Put message in the filer chain");
            chain.doFilter(request, response);
        } else {
            logger.debug("Forward to default Servlet");
            defaultRequestDispatcher.forward(request, response);
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.defaultRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("default");
        this.salRestRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("iServe SAL REST Endpoint");
    }
}
