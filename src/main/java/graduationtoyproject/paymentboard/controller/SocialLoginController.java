package graduationtoyproject.paymentboard.controller;

import graduationtoyproject.paymentboard.auth.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class SocialLoginController {

    private final JwtUtil jwtUtil;

    @GetMapping("/")
    public ResponseEntity<String> main() {

        return ResponseEntity.ok("main");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {

        return ResponseEntity.ok("login test page");
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refreshToken = cookie.getValue();
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("refresh token is null");
        }

        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {
            return ResponseEntity.badRequest().body("refresh token is invalid");
        }

        String username = jwtUtil.getUsername(refreshToken);
        String role = jwtUtil.getRole(refreshToken);

        String newAccessToken = jwtUtil.createJwt("access", username, role, 10 * 60L);
        String newRefreshToken = jwtUtil.createJwt(refreshToken, username, role, 36 * 60 * 60L);

        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.addCookie(createCookie("refresh", newRefreshToken));

        return ResponseEntity.ok(newAccessToken);
    }

    private Cookie createCookie(String key, String value) {

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(36 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
