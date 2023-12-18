package hexlet.code.app.component;

import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.CustomUserDetailsService;
import hexlet.code.app.util.TaskStatusUtils;
import hexlet.code.app.util.UserUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskStatusUtils taskStatusUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var admin = userUtils.getAdmin();
        userService.createUser(admin);

        var draftStatus = taskStatusUtils.getTaskStatus(TaskStatusUtils.DRAFT_NAME, TaskStatusUtils.DRAFT_SLUG);
        taskStatusRepository.save(draftStatus);

        var toReviewStatus = taskStatusUtils.getTaskStatus(TaskStatusUtils.TO_REVIEW_NAME,
                TaskStatusUtils.TO_REVIEW_SLUG);
        taskStatusRepository.save(toReviewStatus);

        var toBeFixedStatus = taskStatusUtils.getTaskStatus(TaskStatusUtils.TO_BE_FIXED_NAME,
                TaskStatusUtils.TO_BE_FIXED_SLUG);
        taskStatusRepository.save(toBeFixedStatus);

        var toPublishStatus = taskStatusUtils.getTaskStatus(TaskStatusUtils.TO_PUBLISH_NAME,
                TaskStatusUtils.TO_PUBLISH_SLUG);
        taskStatusRepository.save(toPublishStatus);

        var publishedStatus = taskStatusUtils.getTaskStatus(TaskStatusUtils.PUBLISHED_NAME,
                TaskStatusUtils.PUBLISHED_SLUG);
        taskStatusRepository.save(publishedStatus);
    }
}
