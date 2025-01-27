package graduationtoyproject.paymentboard.auth.oauth;

import graduationtoyproject.paymentboard.auth.CookieUtil;
import graduationtoyproject.paymentboard.auth.RefreshTokenHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@AllArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final RefreshTokenHelper refreshTokenHelper;
    private final CookieUtil cookieUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        if (!isLogoutRequest(request)) {

            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = cookieUtil.getCookieValue(request, "refresh");

        try {
            refreshTokenHelper.validateRefreshToken(refreshToken);
        } catch (IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (!refreshTokenHelper.isExistRefreshToken(refreshToken)) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        refreshTokenHelper.deleteRefreshToken(refreshToken);

        Cookie cookie = cookieUtil.createCookie("refresh", null, 0);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean isLogoutRequest(HttpServletRequest request) {

        String requestURI = request.getRequestURI();
        String requestMethod = request.getMethod();

        return requestURI.matches("^/logout$") && requestMethod.equals("GET");
    }

}
