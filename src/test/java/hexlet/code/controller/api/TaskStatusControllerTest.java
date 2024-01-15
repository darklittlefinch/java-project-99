package hexlet.code.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.util.TestUtils;
import hexlet.code.util.UserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import hexlet.code.repository.TaskStatusRepository;

import java.util.HashMap;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TestUtils testUtils;

    private JwtRequestPostProcessor token;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject(UserUtils.ADMIN_EMAIL));
    }

    @AfterEach
    public void clean() {
        testUtils.clean();
    }

    @Test
    public void testIndex() throws Exception {
        var taskStatus1 = testUtils.generateTaskStatus();
        var taskStatus2 = testUtils.generateTaskStatus();

        taskStatusRepository.save(taskStatus1);
        taskStatusRepository.save(taskStatus2);

        var result = mockMvc.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var taskStatuses = om.readValue(body, new TypeReference<List<TaskStatus>>() { });
        var expected = taskStatusRepository.findAll();

        assertThat(taskStatuses).containsAll(expected);
    }

    @Test
    public void testShow() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        var result = mockMvc.perform(get("/api/task_statuses/" + taskStatus.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isPresent(),
                json -> json.node("name").isEqualTo(taskStatus.getName()),
                json -> json.node("slug").isEqualTo(taskStatus.getSlug()),
                json -> json.node("createdAt").isPresent()
        );

        var receivedTaskStatus = om.readValue(body, TaskStatus.class);
        assertThat(receivedTaskStatus).isEqualTo(taskStatus);
    }

    @Test
    public void testCreate() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskStatus));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isPresent(),
                json -> json.node("name").isEqualTo(taskStatus.getName()),
                json -> json.node("slug").isEqualTo(taskStatus.getSlug()),
                json -> json.node("createdAt").isPresent()
        );

        var id = om.readTree(body).get("id").asLong();
        assertThat(taskStatusRepository.findById(id)).isPresent();
    }

    @Test
    public void testCreateWithoutAuthorization() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();

        var request = post("/api/task_statuses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskStatus));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isEmpty();
    }

    @Test
    public void testUpdate() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        var oldSlug = taskStatus.getSlug();
        var newSlug = "newSlug";

        var data = new HashMap<>();
        data.put("slug", newSlug);

        var request = put("/api/task_statuses/" + taskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        taskStatus = taskStatusRepository.findById(taskStatus.getId()).get();

        assertThat(taskStatus.getSlug()).isEqualTo(newSlug);
        assertThat(taskStatusRepository.findBySlug(oldSlug)).isEmpty();
        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isPresent();
    }

    @Test
    public void testUpdateWithoutAuthorization() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        var newSlug = "newSlug";

        var data = new HashMap<>();
        data.put("slug", newSlug);

        var request = put("/api/task_statuses/" + taskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isPresent();
        assertThat(taskStatusRepository.findBySlug(newSlug)).isEmpty();
    }

    @Test
    public void testDestroy() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(taskStatus.getId())).isEmpty();
    }

    @Test
    public void testDestroyWithoutAuthorization() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId()))
                .andExpect(status().isUnauthorized());

        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isPresent();
    }

    @Test
    public void testDestroyButStatusIsUsing() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        var task = testUtils.generateTask();

        taskStatus.getTasks().add(task);
        task.setTaskStatus(taskStatus);

        taskStatusRepository.save(taskStatus);
        taskRepository.save(task);

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId()).with(token))
                .andExpect(status().isInternalServerError());
    }
}
