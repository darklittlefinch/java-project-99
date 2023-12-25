package hexlet.code.app.mapper;

import hexlet.code.app.model.BaseEntity;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import jakarta.persistence.EntityManager;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.TargetType;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class ReferenceMapper {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    public <T extends BaseEntity> T toEntity(Long id, @TargetType Class<T> entityClass) {
        return id != null ? entityManager.find(entityClass, id) : null;
    }

    public TaskStatus toEntity(String slug) {
        var taskStatusOptional = taskStatusRepository.findBySlug(slug);
        return taskStatusOptional.orElse(null);
    }
}
