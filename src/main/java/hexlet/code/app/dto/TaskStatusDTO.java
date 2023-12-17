package hexlet.code.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TaskStatusDTO {
    private String id;
    private String name;
    private String slug;
    private Instant createdAt;
}
