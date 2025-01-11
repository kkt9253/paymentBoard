package graduationtoyproject.paymentboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;

@RestController
public class SocialLoginController {

    @GetMapping("/")
    public ResponseEntity<String> main() {

        return ResponseEntity.ok("main");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {

        return ResponseEntity.ok("login test page");
    }
}
