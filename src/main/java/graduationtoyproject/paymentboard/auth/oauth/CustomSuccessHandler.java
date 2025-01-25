package graduationtoyproject.paymentboard.auth.oauth;

import graduationtoyproject.paymentboard.auth.CookieUtil;
import graduationtoyproject.paymentboard.auth.JwtUtil;
import graduationtoyproject.paymentboard.domain.entity.RefreshToken;
import graduationtoyproject.paymentboard.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@AllArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, SecurityException {

        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();
        String role = authority.getAuthority();

        String accessToken = jwtUtil.createJwt("access", username, role, 10 * 60L);
        String refreshToken = jwtUtil.createJwt("refresh", username, role, 36 * 60 * 60L);

        addRefreshToken(username, refreshToken);

        response.setHeader("Authorization", "Bearer " + accessToken);
        response.addCookie(cookieUtil.createCookie("refresh", refreshToken, 36 * 60 * 60));
        response.sendRedirect("http://localhost:3000/");
    }

    private void addRefreshToken(String username, String refreshToken) {

        RefreshToken refreshTokenEntity = new RefreshToken(username, refreshToken);

        refreshTokenRepository.save(refreshTokenEntity);
    }
}
