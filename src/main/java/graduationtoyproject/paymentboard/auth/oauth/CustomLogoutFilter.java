package graduationtoyproject.paymentboard.auth.oauth;

import graduationtoyproject.paymentboard.auth.CookieUtil;
import graduationtoyproject.paymentboard.auth.JwtUtil;
import graduationtoyproject.paymentboard.domain.entity.RefreshToken;
import graduationtoyproject.paymentboard.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.util.Optional;

@AllArgsConstructor
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        doFilter((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse, filterChain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        if (!requestURI.matches("^/logout$")) {

            filterChain.doFilter(request, response);
            return;
        }
        String requestMethod = request.getMethod();
        if (!requestMethod.equals("GET")) {

            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = jwtUtil.getRefreshToken(request);

        if (refreshToken == null) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh") && !validateRefreshToken(jwtUtil.getUsername(refreshToken), refreshToken)) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        Cookie cookie = cookieUtil.createCookie("refresh", null, 0);
        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean validateRefreshToken(String username, String refreshToken) {

        Optional<RefreshToken> existRefreshToken = refreshTokenRepository.findById(username);
        return existRefreshToken.isPresent() && refreshToken.equals(existRefreshToken.get().getRefreshToken());
    }
}
