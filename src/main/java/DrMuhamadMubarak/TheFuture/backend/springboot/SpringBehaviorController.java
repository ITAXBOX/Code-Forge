package DrMuhamadMubarak.TheFuture.backend.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static DrMuhamadMubarak.TheFuture.backend.springboot.SpringBehaviorControllerUtils.*;

public class SpringBehaviorController {

    public static void generateControllerClass(String projectName,
                                               String entityName,
                                               String behaviorMethods) throws IOException {
        if (projectName == null || entityName == null || behaviorMethods == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        String endpoints = generateEndpoints(behaviorMethods);

        String packagePath = projectName.toLowerCase();
        String entityPath = entityName.toLowerCase();

        String controllerCode = String.format(getControllerTemplate(),
                packagePath,
                packagePath,
                packagePath,
                entityPath,
                entityName,
                entityName,
                endpoints);

        String path = "./" + projectName + "/src/main/java/com/example/" +
                      packagePath + "/behaviorcontrollers/" +
                      entityName + "BehaviorController.java";
        Files.write(Paths.get(path), controllerCode.getBytes());
    }
}