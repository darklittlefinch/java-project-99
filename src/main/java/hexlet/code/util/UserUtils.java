package hexlet.code.util;

import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserUtils {

    public static final String ADMIN_EMAIL = "hexlet@example.com";
    public static final String ADMIN_PASSWORD = "qwerty";

    private final UserRepository userRepository;

    @Bean
    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email).get();
    }

    @Bean
    public User getAdmin() {
        var admin = new User();
        admin.setEmail(UserUtils.ADMIN_EMAIL);
        admin.setPassword(UserUtils.ADMIN_PASSWORD);
        return admin;
    }
}
