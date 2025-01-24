package graduationtoyproject.paymentboard.auth.oauth;

import graduationtoyproject.paymentboard.domain.UserRole;
import graduationtoyproject.paymentboard.domain.dto.NaverResponse;
import graduationtoyproject.paymentboard.domain.dto.OAuth2Response;
import graduationtoyproject.paymentboard.domain.dto.UserDTO;
import graduationtoyproject.paymentboard.domain.entity.User;
import graduationtoyproject.paymentboard.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println("CustomOauth2UserService 호출");
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println("네이버 사용자 정보: " + oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = resolveOAuth2Response(registrationId, oAuth2User);

        if (oAuth2Response == null) {
            throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        }

        String username = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();

        User user = userRepository.findByUsername(username)
                .map(existUser -> handleSocialUserInfo(oAuth2Response, existUser))
                .orElseGet(() -> createNewUser(oAuth2Response, username));
        userRepository.save(user);

        UserDTO userDTO = createUserDTO(user);

        return new CustomOAuth2User(userDTO);
    }


    private OAuth2Response resolveOAuth2Response(String registrationId, OAuth2User oAuth2User) {

        if (registrationId.equals("naver")) {
            return new NaverResponse(oAuth2User.getAttributes());
        }

        return null;
    }

    private User handleSocialUserInfo(OAuth2Response oAuth2Response, User user) {

        user.setName(oAuth2Response.getName());
        user.setEmail(oAuth2Response.getEmail());
        user.setMobile(oAuth2Response.getMobile());
        user.setBirth_year(oAuth2Response.getBirthYear());
        user.setGender(oAuth2Response.getGender());

        return user;
    }

    private User createNewUser(OAuth2Response oAuth2Response, String username) {

        User user = handleSocialUserInfo((oAuth2Response), new User());
        user.setUsername(username);
        user.setRole(UserRole.GUIDE_CONSUMER);

        return user;
    }

    private UserDTO createUserDTO(User user) {

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(user.getUsername());
        userDTO.setName(user.getName());
        userDTO.setRole(user.getRole());

        return userDTO;
    }
}
