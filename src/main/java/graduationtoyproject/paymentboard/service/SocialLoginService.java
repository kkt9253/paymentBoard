package graduationtoyproject.paymentboard.service;

import graduationtoyproject.paymentboard.auth.CookieUtil;
import graduationtoyproject.paymentboard.auth.JwtUtil;
import graduationtoyproject.paymentboard.auth.RefreshTokenHelper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SocialLoginService {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final RefreshTokenHelper refreshTokenHelper;

    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = cookieUtil.getCookieValue(request, "refresh");

        try {
            refreshTokenHelper.validateRefreshToken(refreshToken);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(e.getMessage());
        }

        if (!refreshTokenHelper.isExistRefreshToken(refreshToken)) {

            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body("Invalid refresh token");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createJwt("access", username, role, 10 * 60L);
        String newRefreshToken = jwtUtil.createJwt(refreshToken, username, role, 36 * 60 * 60L);

        refreshTokenHelper.updateRefreshToken(refreshToken);

        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(cookieUtil.createCookie("refresh", newRefreshToken, 36 * 60 * 60));

        return ResponseEntity.ok(newAccessToken);
    }
}
