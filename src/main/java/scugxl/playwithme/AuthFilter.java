package scugxl.playwithme;

import lombok.extern.log4j.*;
import scugxl.playwithme.svc.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/**
 *
 */
@Log4j2
public class AuthFilter implements Filter {

    /**
     * If this is an OPTION, simply return 200?
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest hreq = (HttpServletRequest) request;
            HttpServletResponse hres = (HttpServletResponse) response;
            String uri = hreq.getRequestURI();
            if (uri.equals("/") || uri.equals("/api/baiduloginok")) {
                filterChain.doFilter(request, response);
                return;
            }
            else if (BaiduUtils.currentTokenInfo == null) {
                hres.sendRedirect("/");
                return;
            }
            filterChain.doFilter(request, response);
        }
    }

}
