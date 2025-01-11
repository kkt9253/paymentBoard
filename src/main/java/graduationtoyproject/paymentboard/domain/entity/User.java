package graduationtoyproject.paymentboard.domain.entity;

import graduationtoyproject.paymentboard.domain.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String username; // 제공자_제공자id
    private String name;
    private String email;
    private String mobile;
    private String gender;
    private String birth_year;
    @Enumerated(EnumType.STRING)
    private UserRole role;
}
