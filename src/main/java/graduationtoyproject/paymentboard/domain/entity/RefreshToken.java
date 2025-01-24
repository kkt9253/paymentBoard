package graduationtoyproject.paymentboard.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Getter
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 36*60*60)
public class RefreshToken {

    @Id
    private String id; // {value}:{username}

    private String refreshToken;
}
