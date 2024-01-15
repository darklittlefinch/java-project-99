package hexlet.code.service;

import hexlet.code.dto.labelDto.LabelCreateDTO;
import hexlet.code.dto.labelDto.LabelDTO;
import hexlet.code.dto.labelDto.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;

    private final LabelMapper labelMapper;

    public List<LabelDTO> getAll() {
        return labelRepository.findAll().stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
        return labelMapper.map(label);
    }

    public LabelDTO create(LabelCreateDTO dto) {
        var label = labelMapper.map(dto);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public LabelDTO update(Long id, LabelUpdateDTO dto) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found"));
        labelMapper.update(dto, label);
        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void delete(Long id) {
        labelRepository.deleteById(id);
    }
}
