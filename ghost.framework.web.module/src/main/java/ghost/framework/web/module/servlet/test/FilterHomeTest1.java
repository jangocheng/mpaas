package ghost.framework.web.module.servlet.test;
import ghost.framework.beans.annotation.order.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.Enumeration;

/**
 * package: ghost.framework.undertow.web.module.test
 *
 * @Author: 郭树灿{guo-w541}
 * @link: 手机:13715848993, QQ 27048384
 * @Description:
 * @Date: 0:57 2020/1/30
 */
@Order(1)
@WebFilter(filterName = "FilterHomeTest1", dispatcherTypes = DispatcherType.REQUEST, urlPatterns = "/*")
public class FilterHomeTest1 implements Filter {
    private Log log = LogFactory.getLog(FilterHomeTest1.class);

    public FilterHomeTest1() {

    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.log.info("init:" + filterConfig.toString());
        Enumeration<String> enumeration = filterConfig.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            this.log.info("init(" + enumeration.nextElement() + ")");
        }
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        this.log.info("FilterHomeTest1 before doFilter:" + servletRequest.toString() + "," + servletResponse.toString());
        filterChain.doFilter(servletRequest, servletResponse);
        this.log.info("FilterHomeTest1 after doFilter:" + servletRequest.toString() + "," + servletResponse.toString());
    }

    @Override
    public void destroy() {
        this.log.info("destroy");
    }
}