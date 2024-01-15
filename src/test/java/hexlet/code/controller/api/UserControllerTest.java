package hexlet.code.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.util.UserUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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
        var user = testUtils.generateUser();
        userRepository.save(user);

        var result = mockMvc.perform(get("/api/users").with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var users = om.readValue(body, new TypeReference<List<User>>() { });
        var expected = userRepository.findAll();

        assertThat(users).containsAll(expected);
    }

    @Test
    public void testShow() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);

        var result = mockMvc.perform(get("/api/users/" + user.getId()).with(token))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("id").isEqualTo(user.getId()),
                json -> json.node("firstName").isEqualTo(user.getFirstName()),
                json -> json.node("lastName").isEqualTo(user.getLastName()),
                json -> json.node("email").isEqualTo(user.getEmail()),
                json -> json.node("createdAt").isEqualTo(user.getCreatedAt().format(TestUtils.FORMATTER))
        );

        var receivedUser = om.readValue(body, User.class);
        assertThat(receivedUser).isEqualTo(user);
    }

    @Test
    public void testCreate() throws Exception {
        var user = testUtils.generateUser();
        var userPassword = user.getPassword();

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(user));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        var id = om.readTree(body).get("id").asLong();
        assertThat(userRepository.findById(id)).isPresent();

        var addedUser = userRepository.findById(id).get();

        assertThatJson(body).and(
                json -> json.node("id").isEqualTo(addedUser.getId()),
                json -> json.node("firstName").isEqualTo(addedUser.getFirstName()),
                json -> json.node("lastName").isEqualTo(addedUser.getLastName()),
                json -> json.node("email").isEqualTo(addedUser.getEmail()),
                json -> json.node("createdAt").isEqualTo(addedUser.getCreatedAt().format(TestUtils.FORMATTER))
        );

        var userHashedPassword = addedUser.getPassword();
        assertThat(userPassword).isNotEqualTo(userHashedPassword);
    }

    @Test
    public void testUpdate() throws Exception {
        var user = testUtils.generateUser();
        var userPassword = user.getPassword();
        userRepository.save(user);

        var oldEmail = user.getEmail();
        var newEmail = "new@gmail.com";
        var newName = "Elisa";

        token = jwt().jwt(builder -> builder.subject(oldEmail));

        var data = new HashMap<>();
        data.put("email", newEmail);
        data.put("firstName", newName);

        var request = put("/api/users/" + user.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        user = userRepository.findById(user.getId()).get();

        assertThat(user.getEmail()).isEqualTo(newEmail);
        assertThat(user.getFirstName()).isEqualTo(newName);
        assertThat(userRepository.findByEmail(oldEmail)).isEmpty();
        assertThat(userRepository.findByEmail(newEmail).get()).isEqualTo(user);

        var userHashedPassword = user.getPassword();
        assertThat(userPassword).isNotEqualTo(userHashedPassword);
    }

    @Test
    public void testUpdateWrongUser() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);

        var oldEmail = user.getEmail();
        var newEmail = "new@gmail.com";

        var data = new HashMap<>();
        data.put("email", newEmail);

        var request = put("/api/users/" + user.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isInternalServerError());

        assertThat(userRepository.findByEmail(oldEmail)).isPresent();
        assertThat(userRepository.findByEmail(newEmail)).isEmpty();
    }

    @Test
    public void testDestroy() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));

        mockMvc.perform(delete("/api/users/" + user.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    public void testDestroyWrongUser() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + user.getId()).with(token))
                .andExpect(status().isInternalServerError());

        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    public void testDestroyButUserHasTasks() throws Exception {
        var task = testUtils.generateTask();
        taskRepository.save(task);

        var user = task.getAssignee();
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));

        mockMvc.perform(delete("/api/users/" + user.getId()).with(token))
                .andExpect(status().isInternalServerError());
    }
}
