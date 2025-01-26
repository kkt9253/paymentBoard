package graduationtoyproject.paymentboard.service;

import graduationtoyproject.paymentboard.auth.CookieUtil;
import graduationtoyproject.paymentboard.auth.JwtUtil;
import graduationtoyproject.paymentboard.domain.entity.RefreshToken;
import graduationtoyproject.paymentboard.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SocialLoginService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;

    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("reissue2 실행");

        String refreshToken = jwtUtil.getRefreshToken(request);

        if (refreshToken == null) {

            return ResponseEntity.badRequest().body("refresh token is null");
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh") && !validateRefreshToken(jwtUtil.getUsername(refreshToken), refreshToken)) {
            return ResponseEntity.badRequest().body("refresh token is invalid");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createJwt("access", username, role, 10 * 60L);
        String newRefreshToken = jwtUtil.createJwt(refreshToken, username, role, 36 * 60 * 60L);

        updateRefreshEntity(username, newRefreshToken);

        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(cookieUtil.createCookie("refresh", newRefreshToken, 36 * 60 * 60));

        return ResponseEntity.ok(newAccessToken);
    }

    private boolean validateRefreshToken(String username, String refreshToken) {

        Optional<RefreshToken> existRefreshToken = refreshTokenRepository.findById(username);
        return existRefreshToken.isPresent() && refreshToken.equals(existRefreshToken.get().getRefreshToken());
    }

    private void updateRefreshEntity(String username, String refreshToken) {

        refreshTokenRepository.deleteByRefreshToken(refreshToken);

        refreshTokenRepository.save(new RefreshToken(username, refreshToken));
    }
}
