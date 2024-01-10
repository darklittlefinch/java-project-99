package hexlet.code.component;

import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.CustomUserDetailsService;
import hexlet.code.util.LabelUtils;
import hexlet.code.util.TaskStatusUtils;
import hexlet.code.util.UserUtils;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final TaskStatusRepository taskStatusRepository;

    private final LabelRepository labelRepository;

    private final CustomUserDetailsService userService;

    private final UserUtils userUtils;

    private final TaskStatusUtils taskStatusUtils;

    private final LabelUtils labelUtils;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var admin = userUtils.getAdmin();
        userService.createUser(admin);

        var defaultTaskStatuses = taskStatusUtils.getDefaultTaskStatuses();

        for (var status: defaultTaskStatuses) {
            taskStatusRepository.save(status);
        }

        var defaultLabels = labelUtils.getDefaultLabels();

        for (var label: defaultLabels) {
            labelRepository.save(label);
        }
    }
}
