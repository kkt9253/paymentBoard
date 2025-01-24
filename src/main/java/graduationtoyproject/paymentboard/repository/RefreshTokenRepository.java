package graduationtoyproject.paymentboard.repository;

import graduationtoyproject.paymentboard.domain.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    void deleteByRefreshToken(String refreshToken);
}
