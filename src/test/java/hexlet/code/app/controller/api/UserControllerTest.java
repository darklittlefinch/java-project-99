package hexlet.code.app.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import hexlet.code.app.util.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

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
        mockMvc.perform(get("/api/users").with(token))
                .andExpect(status().isOk());
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
                json -> json.node("id").isPresent(),
                json -> json.node("firstName").isEqualTo(user.getFirstName()),
                json -> json.node("lastName").isEqualTo(user.getLastName()),
                json -> json.node("email").isEqualTo(user.getEmail()),
                json -> json.node("createdAt").isPresent()
        );
    }

    @Test
    public void testCreate() throws Exception {
        var usersCount = userRepository.count();

        var user = testUtils.generateUser();
        var userPassword = user.getPassword();

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(user));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        assertThat(userRepository.count()).isEqualTo(usersCount + 1);

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(
                json -> json.node("firstName").isEqualTo(user.getFirstName()),
                json -> json.node("lastName").isEqualTo(user.getLastName()),
                json -> json.node("email").isEqualTo(user.getEmail())
        );

        assertThat(userRepository.findByEmail(user.getEmail())).isPresent();

        var userHashedPassword = userRepository.findByEmail(user.getEmail()).get().getPassword();
        assertThat(userPassword).isNotEqualTo(userHashedPassword);
    }

    @Test
    public void testUpdate() throws Exception {
        var user = testUtils.generateUser();
        var userPassword = user.getPassword();
        userRepository.save(user);

        var usersCount = userRepository.count();
        var oldEmail = user.getEmail();
        token = jwt().jwt(builder -> builder.subject(oldEmail));

        var data = new HashMap<>();
        data.put("email", "new@gmail.com");
        data.put("firstName", "Elisa");

        var request = put("/api/users/" + user.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        user = userRepository.findById(user.getId()).get();

        assertThat(userRepository.count()).isEqualTo(usersCount);
        assertThat(user.getEmail()).isEqualTo("new@gmail.com");
        assertThat(user.getFirstName()).isEqualTo("Elisa");
        assertThat(userRepository.findByEmail(oldEmail)).isEmpty();

        var userHashedPassword = userRepository.findByEmail(user.getEmail()).get().getPassword();
        assertThat(userPassword).isNotEqualTo(userHashedPassword);
    }

    @Test
    public void testUpdateWrongUser() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);
        var oldEmail = user.getEmail();

        var data = new HashMap<>();
        data.put("email", "new@gmail.com");

        var request = put("/api/users/" + user.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isForbidden());

        assertThat(userRepository.findByEmail(oldEmail)).isPresent();
        assertThat(userRepository.findByEmail("new@gmail.com")).isEmpty();
    }

    @Test
    public void testDestroy() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);
        token = jwt().jwt(builder -> builder.subject(user.getEmail()));

        var usersCount = userRepository.count();

        mockMvc.perform(delete("/api/users/" + user.getId()).with(token))
                .andExpect(status().isNoContent());

        assertThat(userRepository.count()).isEqualTo(usersCount - 1);
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    public void testDestroyWrongUser() throws Exception {
        var user = testUtils.generateUser();
        userRepository.save(user);

        var usersCount = userRepository.count();

        mockMvc.perform(delete("/api/users/" + user.getId()).with(token))
                .andExpect(status().isForbidden());

        assertThat(userRepository.count()).isEqualTo(usersCount);
        assertThat(userRepository.findById(user.getId())).isPresent();
    }

    @Test
    public void testIsAdminPresent() {
        assertThat(userRepository.findByEmail("hexlet@example.com")).isPresent();
    }

}
