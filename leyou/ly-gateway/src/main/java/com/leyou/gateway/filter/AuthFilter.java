package com.leyou.gateway.filter;

import com.leyou.auth.entiy.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.AllowProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * zuul网关转发请求前先校验cookie中是否有token
 * 若有token则使用jwt工具类校验,只有校验成功(已登录)才转发到微服务,否则拦截返回403
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, AllowProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private AllowProperties allowProperties;

    @Override
    public String filterType() {
        //前置过滤器
        return FilterConstants.PRE_TYPE;
    }

    /**
     * PRE：在请求被路由之前调用，可以使用这种过滤器实现身份验证、在集群中选择请求的微服务、记录调试Log等。
     * ROUTE：将请求路由到对应的微服务，用于构建发送给微服务的请求。
     * POST：在请求被路由到对应的微服务以后执行，可用来为Response添加HTTP Header、将微服务的Response发送给客户端等。
     * ERROR：在其他阶段发生错误时执行该过滤器。
     */
    @Override
    public int filterOrder() {
        //过滤器顺序
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;
    }

    /**
     * 是否过滤
     * @return 若在白名单中的地址则不过滤return false
     */
    @Override
    public boolean shouldFilter() {
        //1、取得request
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        //2、遍历白名单判断请求uri的前缀是否匹配
        String uri = request.getRequestURI();
//        for (String path : allowProperties.getAllowPaths()) {
//            if(uri.startsWith(path)){
//                return false;
//            }
//        }
//        return true;

        //anyMatch用于判断流中是否存在至少一个元素满足指定的条件
        boolean isAllow = allowProperties.getAllowPaths().stream()
                .anyMatch(path -> uri.startsWith(path));
        //若白名单则返回false (放行)
        return !isAllow;
    }

    /**
     * 过滤逻辑
     * @return 不管返回什么都是默认放行请求
     */
    @Override
    public Object run() throws ZuulException {
        //1、取得request
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();
        //2、从cookie取得token
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            //3、解析token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
            //TODO 校验用户权限
        }catch (Exception e){
            //解析token失败(没有读取到token)，未登录状态，拦截请求
            context.setSendZuulResponse(false);
            //访问拒绝状态码
            context.setResponseStatusCode(HttpStatus.SC_FORBIDDEN);
        }

        return null; //不管返回什么都是默认放行请求
    }
}
