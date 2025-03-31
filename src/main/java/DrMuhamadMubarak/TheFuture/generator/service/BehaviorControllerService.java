package DrMuhamadMubarak.TheFuture.generator.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@AllArgsConstructor
public class BehaviorControllerService {
    private final EntityCodeGeneratorService entityCodeGeneratorService;

    public void generateControllerBehaviors(String projectName, String entityName, String validatedMethods) throws IOException {
        entityCodeGeneratorService.generateBehaviorControllerClass(
                projectName,
                entityName,
                validatedMethods
        );
    }
}
