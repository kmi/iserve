package uk.ac.open.kmi.iserve.elda;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by Luca Panziera on 17/09/2014.
 */
public class DefaultFilter implements Filter {

    private RequestDispatcher defaultRequestDispatcher;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        defaultRequestDispatcher.forward(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.defaultRequestDispatcher =
                filterConfig.getServletContext().getNamedDispatcher("default");
    }
}
