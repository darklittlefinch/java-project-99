package hexlet.code.util;

import hexlet.code.model.TaskStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskStatusUtils {
    @Bean
    public List<TaskStatus> getDefaultTaskStatuses() {
        var draftStatus = createTaskStatus("Draft", "draft");
        var toReviewStatus = createTaskStatus("To review", "to_review");
        var toBeFixedStatus = createTaskStatus("To be fixed", "to_be_fixed");
        var toPublishStatus = createTaskStatus("To publish", "to_publish");
        var publishedStatus = createTaskStatus("Published", "published");

        return List.of(
                draftStatus,
                toReviewStatus,
                toBeFixedStatus,
                toPublishStatus,
                publishedStatus
        );
    }

    private TaskStatus createTaskStatus(String name, String slug) {
        var taskStatus = new TaskStatus();

        taskStatus.setName(name);
        taskStatus.setSlug(slug);

        return taskStatus;
    }
}
