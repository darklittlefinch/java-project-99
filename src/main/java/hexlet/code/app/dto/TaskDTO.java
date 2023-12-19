package hexlet.code.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Long index;
    private Instant createdAt;
    private Long assigneeId;
    private String title;
    private String content;
    private String statusSlug;
}
