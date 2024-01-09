package hexlet.code.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.util.TestUtils;
import hexlet.code.util.UserUtils;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import hexlet.code.repository.TaskRepository;

import java.util.HashMap;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

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
    public void testIndexFiltered() throws Exception {
        var task = testUtils.generateTask();
        var titleCont = task.getName().substring(1).toLowerCase();
        var assigneeId = task.getAssignee().getId();
        var status = task.getTaskStatus().getSlug();
        var labelId = task.getLabels().iterator().next().getId();

        taskRepository.save(task);

        var taskWrong = testUtils.generateTask();
        taskRepository.save(taskWrong);

        var request = get("/api/tasks"
                + "?"
                + "titleCont=" + titleCont
                + "&assigneeId=" + assigneeId
                + "&status=" + status
                + "&labelId=" + labelId)
                .with(token);

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var data = new HashMap<>();
        data.put("assignee_id", task.getAssignee().getId());
        data.put("content", task.getDescription());
        data.put("createdAt", "${json-unit.ignore}");
        data.put("id", task.getId());
        data.put("index", task.getIndex());
        data.put("status", task.getTaskStatus().getSlug());
        data.put("title", task.getName());
        data.put("taskLabelIds", List.of(labelId));

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).when(Option.IGNORING_ARRAY_ORDER)
                .isArray()
                .contains(om.writeValueAsString(data));
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
                json -> json.node("status").isEqualTo(task.getTaskStatus().getSlug()),
                json -> json.node("createdAt").isPresent()
        );
    }

    @Test
    public void testCreate() throws Exception {
        var tasksCount = taskRepository.count();
        var task = testUtils.generateTask();

        var data = new HashMap<>();
        data.put("title", task.getName());
        data.put("index", task.getIndex());
        data.put("content", task.getDescription());
        data.put("assignee_id", task.getAssignee().getId());
        data.put("status", task.getTaskStatus().getSlug());

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

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
                json -> json.node("status").isEqualTo(task.getTaskStatus().getSlug()),
                json -> json.node("createdAt").isPresent()
        );

        assertThat(taskRepository.findByName(task.getName())).isPresent();
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
        assertThat(task.getName()).isEqualTo("new title");
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
