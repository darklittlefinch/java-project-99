package hexlet.code.app.util;

import hexlet.code.app.model.Label;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.model.Task;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

    @Autowired
    private Faker faker;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private TaskStatusUtils taskStatusUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private LabelRepository labelRepository;

    @Bean
    public User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPassword), () -> faker.internet().password())
                .create();
    }

    @Bean
    public TaskStatus generateTaskStatus() {
        var slug = getRandomUniqueTaskStatusSlug();
        return Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .supply(Select.field(TaskStatus::getSlug), () -> slug)
                .supply(Select.field(TaskStatus::getName), () -> slug)
                .create();
    }

    private String getRandomUniqueTaskStatusSlug() {
        var slug = "";
        do {
            slug = faker.lorem().word().toLowerCase();
        } while (taskStatusRepository.findBySlug(slug).isPresent());
        return slug;
    }

    @Bean
    public Task generateTask() {
        var task = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getAssignee))
                .ignore(Select.field(Task::getTaskStatus))
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getIndex), () -> faker.number().randomNumber())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .create();

        var assignee = generateUser();
        userRepository.save(assignee);
        task.setAssignee(assignee);

        var taskStatus = generateTaskStatus();
        taskStatusRepository.save(taskStatus);
        task.setTaskStatus(taskStatus);

        return task;
    }

    @Bean
    public Label generateLabel() {
        var name = getRandomUniqueLabelName();
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), () -> name)
                .create();
    }

    private String getRandomUniqueLabelName() {
        var name = "";
        do {
            name = faker.lorem().word().toLowerCase();
        } while (labelRepository.findByName(name).isPresent());
        return name;
    }
}
