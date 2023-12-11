package hexlet.code.app.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
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
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    public User generateUser() {
        return Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getPasswordDigest), () -> faker.internet().password())
                .create();
    }

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
    }

    @Test
    public void testShow() throws Exception {
        var user = generateUser();
        userRepository.save(user);

        var result = mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isNotNull().and(
                json -> json.node("firstName").isEqualTo(user.getFirstName()),
                json -> json.node("lastName").isEqualTo(user.getLastName()),
                json -> json.node("email").isEqualTo(user.getEmail()),
                json -> json.node("createdAt").isEqualTo(user.getCreatedAt())
        );
    }

    @Test
    public void testCreate() throws Exception {
        var usersCount = userRepository.count();

        var user = generateUser();

        var request = post("/users")
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
    }

    @Test
    public void testUpdate() throws Exception {
        var user = generateUser();
        userRepository.save(user);

        var usersCount = userRepository.count();
        var oldEmail = user.getEmail();

        var data = new HashMap<>();
        data.put("email", "new@gmail.com");

        var request = put("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(data));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        user = userRepository.findById(user.getId()).get();

        assertThat(userRepository.count()).isEqualTo(usersCount);
        assertThat(user.getEmail()).isEqualTo("new@gmail.com");
        assertThat(userRepository.findByEmail(oldEmail)).isEmpty();
    }

    @Test
    public void testDestroy() throws Exception {
        var user = generateUser();
        userRepository.save(user);

        var usersCount = userRepository.count();

        mockMvc.perform(delete("/users/" + user.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.count()).isEqualTo(usersCount - 1);
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    public void testAdmin() {
        assertThat(userRepository.findByEmail("hexlet@example.com")).isPresent();
    }
}
