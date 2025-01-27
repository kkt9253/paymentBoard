package graduationtoyproject.paymentboard.auth;

import graduationtoyproject.paymentboard.domain.entity.RefreshToken;
import graduationtoyproject.paymentboard.repository.RefreshTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class RefreshTokenHelper {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public void validateRefreshToken(String refreshToken) {

        if (refreshToken == null) {

            throw new IllegalStateException("refresh token is null");
        }

        if (jwtUtil.isExpired(refreshToken)) {

            throw new IllegalStateException("refresh token has expired");
        }

        String category = jwtUtil.getCategory(refreshToken);
        if (!category.equals("refresh")) {

            throw new IllegalStateException("Invalid refresh token category");
        }
    }

    public boolean isExistRefreshToken(String refreshToken) {

        String username = jwtUtil.getUsername(refreshToken);

        Optional<RefreshToken> existRefreshToken = refreshTokenRepository.findById(username);
        return existRefreshToken.isPresent() && existRefreshToken.get().getRefreshToken().equals(refreshToken);
    }

    public void updateRefreshToken(String refreshToken) {

        String username = jwtUtil.getUsername(refreshToken);

        refreshTokenRepository.deleteById(username);
        refreshTokenRepository.save(new RefreshToken(username, refreshToken));
    }

    public void deleteRefreshToken(String refreshToken) {

        String username = jwtUtil.getUsername(refreshToken);

        refreshTokenRepository.deleteById(username);
    }
}
