package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.backend.springboot.SpringStructure;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class ProjectStructureService {

    public void generateProjectStructure(String projectName, String frontendType, String backendType, String databaseType) throws IOException {
        SpringStructure.generateSpringBootProjectStructure(projectName, frontendType, databaseType);
    }

}

