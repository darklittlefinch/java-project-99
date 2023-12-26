package hexlet.code.app.dto.taskStatusDto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TaskStatusDTO {
    private Long id;
    private String name;
    private String slug;
    private Instant createdAt;
}