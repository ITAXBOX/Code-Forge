package DrMuhamadMubarak.TheFuture.Generator.Service;

import DrMuhamadMubarak.TheFuture.SpringBoot.SpringStructure;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class ProjectGenerator {

    public void generateProjectStructure(String projectName, String frontendType, String backendType, String databaseType) throws IOException {
        SpringStructure.generateSpringBootProjectStructure(projectName, frontendType, databaseType);
    }

}

