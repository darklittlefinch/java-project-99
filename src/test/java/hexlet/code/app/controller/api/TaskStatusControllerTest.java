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
import hexlet.code.app.repository.TaskStatusRepository;

import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

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
        mockMvc.perform(get("/api/task_statuses").with(token))
                .andExpect(status().isOk());
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
    }

    @Test
    public void testCreate() throws Exception {
        var taskStatusesCount = taskStatusRepository.count();
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

        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isPresent();
        assertThat(taskStatusRepository.count()).isEqualTo(taskStatusesCount + 1);
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
        var taskStatusesCount = taskStatusRepository.count();

        var data = new HashMap<>();
        data.put("slug", "newSlug");

        var request = put("/api/task_statuses/" + taskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        taskStatus = taskStatusRepository.findById(taskStatus.getId()).get();

        assertThat(taskStatusRepository.count()).isEqualTo(taskStatusesCount);
        assertThat(taskStatus.getSlug()).isEqualTo("newSlug");
        assertThat(taskStatusRepository.findBySlug(oldSlug)).isEmpty();
        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isPresent();
    }

    @Test
    public void testUpdateWithoutAuthorization() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        var data = new HashMap<>();
        data.put("slug", "newSlug");

        var request = put("/api/task_statuses/" + taskStatus.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        assertThat(taskStatusRepository.findBySlug(taskStatus.getSlug())).isPresent();
        assertThat(taskStatusRepository.findBySlug("newSlug")).isEmpty();
    }

    @Test
    public void testDestroy() throws Exception {
        var taskStatus = testUtils.generateTaskStatus();
        taskStatusRepository.save(taskStatus);

        var taskStatusesCount = taskStatusRepository.count();

        mockMvc.perform(delete("/api/task_statuses/" + taskStatus.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.count()).isEqualTo(taskStatusesCount - 1);
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
}
