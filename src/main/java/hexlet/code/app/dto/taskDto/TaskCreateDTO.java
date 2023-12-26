package hexlet.code.app.dto.taskDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    @NotBlank
    private String title;

    @Column(unique = true)
    private Long index;

    private String content;

    private Long assigneeId;

    @NotNull
    private String status;
}
