package hexlet.code.app.dto.userDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class UserUpdateDTO {
    @Email
    @Column(unique = true)
    private JsonNullable<String> email;


    private JsonNullable<String> firstName;

    private JsonNullable<String> lastName;

    @NotBlank
    private JsonNullable<String> password;
}
