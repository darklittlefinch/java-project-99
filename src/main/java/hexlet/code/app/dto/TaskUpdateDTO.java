package hexlet.code.app.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskUpdateDTO {
    @NotBlank
    private JsonNullable<String> title;

    @Column(unique = true)
    private JsonNullable<Long> index;

    private JsonNullable<String> content;

    private JsonNullable<Long> assigneeId;

    @NotNull
    private JsonNullable<String> status;
}
