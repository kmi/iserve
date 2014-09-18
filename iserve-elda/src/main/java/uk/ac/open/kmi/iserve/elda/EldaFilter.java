package uk.ac.open.kmi.iserve.elda;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Luca Panziera on 17/09/2014.
 */
public class EldaFilter implements Filter {

    private RequestDispatcher defaultRequestDispatcher;
    private RequestDispatcher salRestRequestDispatcher;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getRequestURI().matches("/iserve/id.*") && !httpRequest.getMethod().equalsIgnoreCase("GET")) {
            salRestRequestDispatcher.forward(request, response);
        } else if (httpRequest.getRequestURI().matches("/iserve/id.*") && httpRequest.getMethod().equalsIgnoreCase("GET")) {
            chain.doFilter(request, response);
        } else {
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
