package hexlet.code.service;

import hexlet.code.dto.taskDto.TaskCreateDTO;
import hexlet.code.dto.taskDto.TaskDTO;
import hexlet.code.dto.taskDto.TaskParamsDTO;
import hexlet.code.dto.taskDto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import hexlet.code.specification.TaskSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private TaskSpecification specBuilder;

    public List<TaskDTO> getAll(TaskParamsDTO params) {
        var specification = specBuilder.build(params);
        return taskRepository.findAll(specification).stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return taskMapper.map(task);
    }

    public TaskDTO create(TaskCreateDTO dto) {
        var task = taskMapper.map(dto);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public TaskDTO update(Long id, TaskUpdateDTO dto) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        taskMapper.update(dto, task);
        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}
