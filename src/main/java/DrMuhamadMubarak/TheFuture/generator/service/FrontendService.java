package DrMuhamadMubarak.TheFuture.generator.service;

import DrMuhamadMubarak.TheFuture.frontend.nextjs.NextjsGenerator;
import org.springframework.stereotype.Service;

@Service
public class FrontendService {
    public void createFrontendFiles(String projectName) {
        NextjsGenerator.generateNextjsFiles(projectName);
    }
}
