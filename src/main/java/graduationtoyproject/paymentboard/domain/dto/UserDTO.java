package graduationtoyproject.paymentboard.domain.dto;

import graduationtoyproject.paymentboard.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

    private String name;
    private String username;
    private UserRole role;
}
