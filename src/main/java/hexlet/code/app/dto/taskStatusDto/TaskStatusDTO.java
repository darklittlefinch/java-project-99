package hexlet.code.app.dto.taskStatusDto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskStatusDTO {
    private Long id;
    private String name;
    private String slug;

    @JsonFormat(pattern = "dd/MM/yyyy, HH:mm:ss")
    private LocalDateTime createdAt;
}
