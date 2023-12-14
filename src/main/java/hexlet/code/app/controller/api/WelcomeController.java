package hexlet.code.app.controller.api;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/welcome")
public class WelcomeController {

    @GetMapping(path = "")
    public String welcome() {
        return "Welcome to Spring!";
    }

}
