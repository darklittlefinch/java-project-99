package hexlet.code.app.util;

import hexlet.code.app.model.TaskStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TaskStatusUtils {
    public static final String DRAFT_NAME = "Draft...";
    public static final String DRAFT_SLUG = "draft";

    public static final String TO_REVIEW_NAME = "To review...";
    public static final String TO_REVIEW_SLUG = "to_review";

    public static final String TO_BE_FIXED_NAME = "To be fixed...";
    public static final String TO_BE_FIXED_SLUG = "to_be_fixed";

    public static final String TO_PUBLISH_NAME = "To publish...";
    public static final String TO_PUBLISH_SLUG = "to_publish";

    public static final String PUBLISHED_NAME = "Published!";
    public static final String PUBLISHED_SLUG = "published";

    @Bean
    public TaskStatus getTaskStatus(String name, String slug) {
        var taskStatus = new TaskStatus();

        taskStatus.setName(name);
        taskStatus.setSlug(slug);

        return taskStatus;
    }
}
