package hexlet.code.app.dto.labelDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class LabelUpdateDTO {

    @NotBlank
    @Column(unique = true)
    private JsonNullable<String> name;
}
