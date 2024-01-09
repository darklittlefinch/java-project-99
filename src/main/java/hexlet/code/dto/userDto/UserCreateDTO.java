package hexlet.code.dto.userDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {

    private String firstName;

    private String lastName;

    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String password;
}
