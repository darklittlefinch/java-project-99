package hexlet.code.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.repository.LabelRepository;
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

import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LabelRepository labelRepository;

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
        taskRepository.deleteAll();
        labelRepository.deleteAll();
    }

    @Test
    public void testIndex() throws Exception {
        mockMvc.perform(get("/api/labels").with(token))
                .andExpect(status().isOk());
    }

    @Test
    public void testShow() throws Exception {
        var label = testUtils.generateLabel();
        labelRepository.save(label);

        var result = mockMvc.perform(get("/api/labels/" + label.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isPresent(),
                json -> json.node("name").isEqualTo(label.getName()),
                json -> json.node("createdAt").isPresent()
        );
    }

    @Test
    public void testCreate() throws Exception {
        var label = testUtils.generateLabel();

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(label));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isPresent(),
                json -> json.node("name").isEqualTo(label.getName()),
                json -> json.node("createdAt").isPresent()
        );

        var id = om.readTree(body).get("id").asLong();
        assertThat(labelRepository.findById(id)).isPresent();
    }

    @Test
    public void testUpdate() throws Exception {
        var label = testUtils.generateLabel();
        labelRepository.save(label);

        var labelsCount = labelRepository.count();

        var data = new HashMap<>();
        data.put("name", "New name");

        var request = put("/api/labels/" + label.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("name").isEqualTo("New name")
        );

        assertThat(labelRepository.count()).isEqualTo(labelsCount);
    }

    @Test
    public void testDestroy() throws Exception {
        var label = testUtils.generateLabel();
        labelRepository.save(label);

        var labelsCount = labelRepository.count();

        mockMvc.perform(delete("/api/labels/" + label.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.count()).isEqualTo(labelsCount - 1);
        assertThat(labelRepository.findById(label.getId())).isEmpty();
    }

    @Test
    public void testDestroyButLabelIsUsing() throws Exception {
        var task = testUtils.generateTask();
        taskRepository.save(task);

        var label = testUtils.generateLabel();
        labelRepository.save(label);

        task.getLabels().add(label);
        label.getTasks().add(task);

        taskRepository.save(task);
        labelRepository.save(label);

        mockMvc.perform(delete("/api/labels/" + label.getId()).with(token))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testSetUpInitialization() {
        assertThat(labelRepository.findByName("feature")).isPresent();
        assertThat(labelRepository.findByName("bug")).isPresent();
    }
}
