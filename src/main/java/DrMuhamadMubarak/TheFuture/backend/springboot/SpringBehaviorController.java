package DrMuhamadMubarak.TheFuture.backend.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SpringBehaviorController {
    private static final String CONTROLLER_TEMPLATE = """
            package com.example.%s.behaviorcontrollers;
            
            import com.example.%s.behaviorservices.*;
            import com.example.%s.models.*;
            import lombok.RequiredArgsConstructor;
            import java.time.*;
            import org.springframework.web.bind.annotation.*;
            import org.springframework.http.HttpStatus;
            import org.springframework.web.server.ResponseStatusException;
            import java.util.*;
            
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
        if (projectName == null || entityName == null || behaviorMethods == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }

        String endpoints = generateEndpoints(behaviorMethods);

        String packagePath = projectName.toLowerCase();
        String entityPath = entityName.toLowerCase();

        String controllerCode = String.format(CONTROLLER_TEMPLATE,
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

    private static String generateEndpoints(String behaviorMethods) {
        StringBuilder endpoints = new StringBuilder();
        // Updated pattern to handle generics in return types and parameters
        Pattern methodPattern = Pattern.compile(
                "@Transactional\\s+public\\s+([\\w<>,\\s]+)\\s+(\\w+)\\(([^)]*)\\)");

        Matcher matcher = methodPattern.matcher(behaviorMethods);

        while (matcher.find()) {
            String returnType = matcher.group(1).trim();
            String methodName = matcher.group(2).trim();
            String params = matcher.group(3).trim();

            endpoints.append(generateEndpoint(returnType, methodName, params));
        }

        return endpoints.toString();
    }

    private static String generateEndpoint(String returnType, String methodName, String params) {
        String basePath = mapMethodToPath(methodName);
        String httpMethod = mapMethodToHttpMethod(methodName);

        List<String> paramList = splitParameters(params);

        StringBuilder pathBuilder = new StringBuilder(basePath);
        List<String> pathVariables = new ArrayList<>();

        String paramMappings = paramList.stream()
                .map(p -> {
                    String[] parts = p.trim().split("\\s+");
                    String name = parts[parts.length - 1];
                    String type = String.join(" ", Arrays.copyOf(parts, parts.length - 1));

                    if (name.equals("id") || name.endsWith("Id") || name.equals("date")) {
                        pathVariables.add(name);
                        return "@PathVariable " + type + " " + name;
                    }
                    return "@RequestParam " + type + " " + name;
                })
                .collect(Collectors.joining(", "));

        if (!pathVariables.isEmpty()) {
            for (String var : pathVariables) {
                pathBuilder.append("/{").append(var).append("}");
            }
        }

        String methodCallArgs = paramList.stream()
                .map(p -> {
                    String[] parts = p.trim().split("\\s+");
                    return parts[parts.length - 1];
                })
                .collect(Collectors.joining(", "));

        boolean isOptional = returnType.startsWith("Optional<");
        String actualReturnType = isOptional
                ? returnType.substring("Optional<".length(), returnType.length() - 1)
                : returnType;

        String returnLine;
        if (returnType.equals("void")) {
            returnLine = "        behaviorService.%s(%s);".formatted(methodName, methodCallArgs);
        } else if (isOptional) {
            returnLine = """
                    return behaviorService.%s(%s)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));"""
                    .formatted(methodName, methodCallArgs);
        } else {
            returnLine = "        return behaviorService.%s(%s);".formatted(methodName, methodCallArgs);
        }

        return """
                @%sMapping("%s")
                public %s %s(%s) {
                %s
                }
                
                """.formatted(
                httpMethod,
                pathBuilder.toString(),
                actualReturnType,
                methodName,
                paramMappings,
                returnLine
        );
    }

    private static List<String> splitParameters(String params) {
        List<String> result = new ArrayList<>();
        if (params == null || params.trim().isEmpty()) {
            return result;
        }

        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : params.toCharArray()) {
            if (c == '<') depth++;
            if (c == '>') depth--;

            if (c == ',' && depth == 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }

        return result;
    }

    private static String mapMethodToPath(String methodName) {
        if (methodName.startsWith("get")) return "/" + methodName.substring(3).toLowerCase();
        if (methodName.startsWith("find")) return "/" + methodName.substring(4).toLowerCase();
        if (methodName.startsWith("update")) return "/" + methodName.substring(6).toLowerCase();
        return "/" + methodName.toLowerCase();
    }

    private static String mapMethodToHttpMethod(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("find")) return "Get";
        if (methodName.startsWith("create") || methodName.startsWith("add")) return "Post";
        if (methodName.startsWith("update")) return "Put";
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) return "Delete";
        return "Post";
    }
}