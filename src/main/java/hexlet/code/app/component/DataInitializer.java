package hexlet.code.app.component;

import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.service.CustomUserDetailsService;
import hexlet.code.app.util.LabelUtils;
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
    private LabelRepository labelRepository;

    @Autowired
    private CustomUserDetailsService userService;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskStatusUtils taskStatusUtils;

    @Autowired
    private LabelUtils labelUtils;

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
