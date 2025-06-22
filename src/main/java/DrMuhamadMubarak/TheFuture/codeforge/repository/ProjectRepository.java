package DrMuhamadMubarak.TheFuture.codeforge.repository;

import DrMuhamadMubarak.TheFuture.codeforge.model.Project;
import org.springframework.data.repository.CrudRepository;

public interface ProjectRepository extends CrudRepository<Project, Long> {

    boolean existsByName(String name);
}
