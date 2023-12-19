package hexlet.code.app.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    @NotBlank
    private String name;

    @Column(unique = true)
    private Long index;

    private String description;

    private Long assigneeId;

    @NotNull
    private String taskStatusSlug;
}
