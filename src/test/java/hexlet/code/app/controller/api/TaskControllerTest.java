package hexlet.code.app.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.util.TestUtils;
import hexlet.code.app.util.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import hexlet.code.app.repository.TaskRepository;

import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/api/tasks").with(token))
                .andExpect(status().isOk());
    }

    @Test
    public void testShow() throws Exception {
        var task = testUtils.generateTask();
        taskRepository.save(task);

        var result = mockMvc.perform(get("/api/tasks/" + task.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isPresent(),
                json -> json.node("title").isEqualTo(task.getName()),
                json -> json.node("index").isEqualTo(task.getIndex()),
                json -> json.node("assignee_id").isEqualTo(task.getAssignee().getId()),
                json -> json.node("content").isEqualTo(task.getDescription()),
                json -> json.node("status").isEqualTo(task.getStatus().getSlug()),
                json -> json.node("createdAt").isPresent()
        );
    }

    @Test
    public void testCreate() throws Exception {
        var tasksCount = taskRepository.count();
        var task = testUtils.generateTask();

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(task));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isPresent(),
                json -> json.node("title").isEqualTo(task.getName()),
                json -> json.node("index").isEqualTo(task.getIndex()),
                json -> json.node("assignee_id").isEqualTo(task.getAssignee().getId()),
                json -> json.node("content").isEqualTo(task.getDescription()),
                json -> json.node("status").isEqualTo(task.getStatus().getSlug()),
                json -> json.node("createdAt").isPresent()
        );

        assertThat(taskRepository.findByIndex(task.getIndex())).isPresent();
        assertThat(taskRepository.count()).isEqualTo(tasksCount + 1);
    }

    @Test
    public void testUpdate() throws Exception {
        var task = testUtils.generateTask();
        taskRepository.save(task);

        var tasksCount = taskRepository.count();

        var data = new HashMap<>();
        data.put("title", "new title");

        var request = put("/api/tasks/" + task.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        task = taskRepository.findById(task.getId()).get();

        assertThat(taskRepository.count()).isEqualTo(tasksCount);
        assertThat(task.getTitle()).isEqualTo("new title");
    }

    @Test
    public void testDestroy() throws Exception {
        var task = testUtils.generateTask();
        taskRepository.save(task);

        var tasksCount = taskRepository.count();

        mockMvc.perform(delete("/api/tasks/" + task.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.count()).isEqualTo(tasksCount - 1);
        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }
}