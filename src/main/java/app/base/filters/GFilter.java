package app.base.filters;

import app.base.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class GFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(GFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        Order order = this.getClass().getDeclaredAnnotation(Order.class);
        logger.info("Initializing filter [{}]: {}",
                order != null ? order.value() : "no order",
                this.getClass().getSimpleName());
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

    }

    @Override
    public void destroy() {
        logger.info("Destructing filter :{}", this.getClass().getSimpleName());
    }

    public static boolean skip(String path, String[] ignoredUrls) {
        if ("/".equals(path)) return false;
        AntPathMatcher matcher = new AntPathMatcher();
        for (String pattern : ignoredUrls) {
            if (matcher.matchStart(pattern, path)) return true;
        }
        return false;
    }

    protected void fillResponse(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().write(SerializationUtils.toJson(body));
    }

}
