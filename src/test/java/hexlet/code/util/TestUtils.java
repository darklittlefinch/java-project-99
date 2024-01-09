package hexlet.code.util;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.model.Task;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;

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

    @Autowired
    private TaskRepository taskRepository;

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

    @Bean
    public Task generateTask() {
        var task = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .ignore(Select.field(Task::getCreatedAt))
                .ignore(Select.field(Task::getAssignee))
                .ignore(Select.field(Task::getTaskStatus))
                .supply(Select.field(Task::getName), this::getRandomUniqueTaskName)
                .supply(Select.field(Task::getIndex), () -> faker.number().randomNumber())
                .supply(Select.field(Task::getDescription), () -> faker.lorem().sentence())
                .supply(Select.field(Task::getLabels), () -> new HashSet<Label>())
                .create();

        var assignee = generateUser();
        userRepository.save(assignee);
        task.setAssignee(assignee);

        var taskStatus = generateTaskStatus();
        taskStatusRepository.save(taskStatus);
        task.setTaskStatus(taskStatus);

        var label = generateLabel();
        labelRepository.save(label);
        task.addLabel(label);

        return task;
    }

    @Bean
    public Label generateLabel() {
        return Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .ignore(Select.field(Label::getCreatedAt))
                .supply(Select.field(Label::getName), this::getRandomUniqueLabelName)
                .supply(Select.field(Label::getTasks), () -> new ArrayList<Task>())
                .create();
    }

    private String getRandomUniqueTaskName() {
        var name = "";
        do {
            name = faker.lorem().word().toLowerCase();
        } while (taskRepository.findByName(name).isPresent());
        return name;
    }

    private String getRandomUniqueTaskStatusSlug() {
        var slug = "";
        do {
            slug = faker.lorem().word().toLowerCase();
        } while (taskStatusRepository.findBySlug(slug).isPresent());
        return slug;
    }

    private String getRandomUniqueLabelName() {
        var name = "";
        do {
            name = faker.lorem().word().toLowerCase();
        } while (labelRepository.findByName(name).isPresent());
        return name;
    }
}
