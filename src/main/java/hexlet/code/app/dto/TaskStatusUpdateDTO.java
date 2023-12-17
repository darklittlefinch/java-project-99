package hexlet.code.app.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskStatusUpdateDTO {

    @NotBlank
    @Column(unique = true)
    private JsonNullable<String> name;

    @NotBlank
    @Column(unique = true)
    private JsonNullable<String> slug;
}
