package graduationtoyproject.paymentboard.auth;

import graduationtoyproject.paymentboard.auth.oauth.CustomOAuth2User;
import graduationtoyproject.paymentboard.domain.UserRole;
import graduationtoyproject.paymentboard.domain.dto.UserDTO;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String accessToken = request.getHeader("Authorization");

        System.out.println("Authorization 헤더: " + accessToken);

        if (accessToken == null || !accessToken.startsWith("Bearer ")) {

            System.out.println("JWT Filter: Missing or invalid Authorization header");
            filterChain.doFilter(request, response);

            // 위에 있을 땐 오류 났음
            //response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            //response.getWriter().write("Invalid Authorization header");

            return;
        }

        String token = accessToken.split(" ")[1];

        try {
            jwtUtil.isExpired(token);
            System.out.println("JWT Filter: Token valid");
        }
        catch (ExpiredJwtException e) {
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            System.out.println("JWT Filter: Error during token validation - " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String category = jwtUtil.getCategory(token);
        if (!category.equals("access")) {

            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        setAuthenticationContext(token);

        filterChain.doFilter(request, response);
    }

    private void setAuthenticationContext(String token) {
        System.out.println("JWT Filter: setAuthenticationContext");
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);

        // nullPointerException
        userDTO.setRole(UserRole.valueOf(role));
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(userDTO);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customOAuth2User, null, customOAuth2User.getAuthorities());
        System.out.println("JWT Filter: SecurityContextHolder");
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}
