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

import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        System.out.println(oAuth2User);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response = null;
        if (registrationId.equals("naver")) {
            oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
        }
        else {
            return null;
        }

        String username = oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId();
        Optional<User> existData = userRepository.findByUsername(username);

        if (existData.isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setName(oAuth2Response.getName());
            user.setEmail(oAuth2Response.getEmail());
            user.setMobile(oAuth2Response.getMobile());
            user.setBirth_year(oAuth2Response.getBirthYear());
            user.setGender(oAuth2Response.getGender());
            user.setRole(UserRole.GUIDE_CONSUMER);

            userRepository.save(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(username);
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(UserRole.GUIDE_CONSUMER);

            return new CustomOAuth2User(userDTO);
        }
        else {
            existData.get().setName(oAuth2Response.getName());
            existData.get().setEmail(oAuth2Response.getEmail());
            existData.get().setMobile(oAuth2Response.getMobile());
            existData.get().setBirth_year(oAuth2Response.getBirthYear());
            existData.get().setGender(oAuth2Response.getGender());

            userRepository.save(existData.get());

            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(existData.get().getUsername());
            userDTO.setName(oAuth2Response.getName());
            userDTO.setRole(existData.get().getRole());

            return new CustomOAuth2User(userDTO);
        }
    }
}
