package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpringBehaviorController {
    private static final String CONTROLLER_TEMPLATE = """
            package com.example.%s.behaviorcontrollers;
            
            import com.example.%s.behaviorServices.*;
            import com.example.%s.models.*;
            import lombok.RequiredArgsConstructor;
            import org.springframework.web.bind.annotation.*;
            
            @RestController
            @RequestMapping("/api/%s")
            @RequiredArgsConstructor
            public class %sBehaviorController {
                private final %sBehaviorService behaviorService;
            
                %s
            }
            """;

    public static void generateControllerClass(String projectName,
                                               String entityName,
                                               String behaviorMethods) throws IOException {
        // Extract endpoints from behavior methods
        String endpoints = generateEndpoints(behaviorMethods);

        String packagePath = projectName.toLowerCase();
        String controllerCode = String.format(CONTROLLER_TEMPLATE,
                packagePath,
                packagePath,
                entityName,
                entityName.toLowerCase(),
                entityName,
                entityName,
                endpoints);

        String path = "./" + projectName + "/src/main/java/com/example/" +
                      packagePath + "/behaviorcontrollers/" +
                      entityName + "BehaviorController.java";
        Files.write(Paths.get(path), controllerCode.getBytes());
    }

    private static String generateEndpoints(String behaviorMethods) {
        // Parse methods and generate corresponding endpoints
        StringBuilder endpoints = new StringBuilder();

        // Pattern to match method signatures
        Pattern methodPattern = Pattern.compile(
                "@Transactional\\s+public\\s+(\\w+)\\s+(\\w+)\\(([^)]*)\\)");

        Matcher matcher = methodPattern.matcher(behaviorMethods);

        while (matcher.find()) {
            String returnType = matcher.group(1);
            String methodName = matcher.group(2);
            String params = matcher.group(3);

            endpoints.append(generateEndpoint(
                    returnType, methodName, params));
        }

        return endpoints.toString();
    }

    private static String generateEndpoint(String returnType, String methodName, String params) {
        String path = mapMethodToPath(methodName);
        String httpMethod = mapMethodToHttpMethod(methodName);
        String paramMappings = Arrays.stream(params.split(","))
                .map(p -> {
                    String[] parts = p.trim().split(" ");
                    String type = parts[0];
                    String name = parts[1];
                    return name.equals("id")
                            ? "@PathVariable " + type + " " + name
                            : "@RequestParam " + type + " " + name;
                })
                .collect(Collectors.joining(", "));

        return """
                @%sMapping("%s")
                public %s %s(%s) {
                    %sbehaviorService.%s(%s);
                }
                """.formatted(
                httpMethod,
                path,
                returnType,
                methodName,
                paramMappings,
                returnType.equals("void") ? "" : "return ",
                methodName,
                Arrays.stream(params.split(","))
                        .map(p -> p.trim().split(" ")[1])
                        .collect(Collectors.joining(", ")));
    }

    // Helper methods
    private static String mapMethodToPath(String methodName) {
        if (methodName.startsWith("get")) return "/" + methodName.substring(3).toLowerCase();
        if (methodName.startsWith("update")) return "/" + methodName.substring(6).toLowerCase();
        return "/" + methodName.toLowerCase();
    }

    private static String mapMethodToHttpMethod(String methodName) {
        if (methodName.startsWith("get")) return "Get";
        if (methodName.startsWith("create") || methodName.startsWith("add")) return "Post";
        if (methodName.startsWith("update")) return "Put";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "Delete";
        return "Post";
    }
}
