package graduationtoyproject.paymentboard.controller;

import graduationtoyproject.paymentboard.service.SocialLoginService;
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

    private final SocialLoginService socialLoginService;

    @GetMapping("/")
    public ResponseEntity<String> home() {

        return ResponseEntity.ok("home");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {

        return ResponseEntity.ok("login test page");
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        System.out.println("reissue1 실행");
        return socialLoginService.reissue(request, response);
    }
}
