package cn.filter;

import cn.common.BaseContext;
import cn.common.R;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebFilter(urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {   //过滤器, 只有登录过的账户才能看到后台数据
    private static final AntPathMatcher MATCHER = new AntPathMatcher();     //路径匹配器

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String uri = request.getRequestURI();

        //使用数组, 将需要过滤的路径存入
        String[] urlPatterns = {
                "/backend/**",
                "/front/**",
                "/employee/login",
                "/employee/logout",
                "/user/login",
                "/user/sendMsg",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources",
                "/v2/api-docs"
        };

        boolean check = check(urlPatterns, uri);

        if (check) {
            filterChain.doFilter(request, response);

            return;
        }

        //若用户没有手动点击退出按钮, 无意退出, 其保存的session说明曾登陆过, 可以直接放行
        if (request.getSession().getAttribute("getUserId") != null) {
            //将id存入本地线程
            BaseContext.set((Long)request.getSession().getAttribute("getUserId"));

            filterChain.doFilter(request, response);
            return;
        }

        if (request.getSession().getAttribute("user") != null) {
            //将id存入本地线程
            BaseContext.set((Long)request.getSession().getAttribute("user"));

            filterChain.doFilter(request, response);
            return;
        }

        response.getWriter().write(JSON.toJSONString(R.fail("NOTLOGIN")));      //响应所需json数据
    }



    public boolean check(String[] urls, String requestUri) {
        for (String url : urls) {
            boolean match = MATCHER.match(url, requestUri);         //判断允许过滤的路径中是否包含当前的请求路径

            if (match)      return true;
        }

        return false;

    }

}