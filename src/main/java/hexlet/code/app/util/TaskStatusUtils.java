package hexlet.code.app.util;

import hexlet.code.app.model.TaskStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskStatusUtils {

    @Bean
    public List<TaskStatus> getDefaultTaskStatuses() {
        var draftStatus = getTaskStatus("Draft...", "draft");
        var toReviewStatus = getTaskStatus("To review...", "to_review");
        var toBeFixedStatus = getTaskStatus("To be fixed...", "to_be_fixed");
        var toPublishStatus = getTaskStatus("To publish...", "to_publish");
        var publishedStatus = getTaskStatus("Published!", "published");

        return List.of(
                draftStatus,
                toReviewStatus,
                toBeFixedStatus,
                toPublishStatus,
                publishedStatus
        );
    }

    private TaskStatus getTaskStatus(String name, String slug) {
        var taskStatus = new TaskStatus();

        taskStatus.setName(name);
        taskStatus.setSlug(slug);

        return taskStatus;
    }
}
