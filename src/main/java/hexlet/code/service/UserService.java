package hexlet.code.service;

import hexlet.code.dto.userDto.UserCreateDTO;
import hexlet.code.dto.userDto.UserDTO;
import hexlet.code.dto.userDto.UserUpdateDTO;
import hexlet.code.exception.AccessDeniedException;
import hexlet.code.exception.AssociatedWithEntityException;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.UserMapper;
import hexlet.code.repository.UserRepository;
import hexlet.code.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<UserDTO> getAll() {
        var users = userRepository.findAll();
        return users.stream()
                .map(userMapper::map)
                .toList();
    }

    public UserDTO findById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return userMapper.map(user);
    }

    public UserDTO create(UserCreateDTO dto) {
        var user = userMapper.map(dto);

        var hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        userRepository.save(user);
        return userMapper.map(user);
    }

    public UserDTO update(UserUpdateDTO dto, Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var currentUser = userUtils.getCurrentUser();

        if (currentUser == null || !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied");
        } else {
            userMapper.update(dto, user);

            var hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);

            userRepository.save(user);
            return userMapper.map(user);
        }
    }

    public void delete(Long id) {
        var currentUser = userUtils.getCurrentUser();

        if (currentUser == null || !currentUser.getId().equals(id)) {
            throw new AccessDeniedException("Access denied");
        } else if (!currentUser.getTasks().isEmpty()) {
            throw new AssociatedWithEntityException("You still have some tasks!");
        }

        userRepository.deleteById(id);
    }
}
