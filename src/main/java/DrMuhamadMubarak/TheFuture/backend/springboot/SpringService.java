package DrMuhamadMubarak.TheFuture.backend.springboot;

import DrMuhamadMubarak.TheFuture.generator.dto.AttributeDTO;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static DrMuhamadMubarak.TheFuture.utils.StringUtils.capitalizeFirstLetter;

public class SpringService {
    public static void generateSpringServiceClass(String projectName, String entityName, List<AttributeDTO> attributes) throws IOException {
        String className = entityName + "Service";
        String fileName = projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/services/" + className + ".java";

        boolean isUserEntity = entityName.equalsIgnoreCase("User");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("package com.example." + projectName.toLowerCase() + ".services;\n\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".repositories." + entityName + "Repository;\n");
            writer.write("import jakarta.persistence.EntityNotFoundException;\n");
            writer.write("import org.springframework.stereotype.Service;\n");

            // Add BCrypt import for User entity
            if (isUserEntity) {
                writer.write("import org.springframework.security.crypto.password.PasswordEncoder;\n");
            }

            writer.write("\nimport java.util.List;\n");
            writer.write("import java.util.Optional;\n\n");

            writer.write("@Service\n");
            writer.write("public class " + className + " {\n\n");

            // Add Repository Field (constructor injection)
            writer.write("    private final " + entityName + "Repository " + entityName.toLowerCase() + "Repository;\n");

            // Add PasswordEncoder field for User entity
            if (isUserEntity) {
                writer.write("    private final PasswordEncoder passwordEncoder;\n\n");
            } else {
                writer.write("\n");
            }

            // Constructor for injection
            writer.write("    public " + className + "(" + entityName + "Repository " + entityName.toLowerCase() + "Repository");

            // Add PasswordEncoder to constructor for User entity
            if (isUserEntity) {
                writer.write(", PasswordEncoder passwordEncoder");
            }

            writer.write(") {\n");
            writer.write("        this." + entityName.toLowerCase() + "Repository = " + entityName.toLowerCase() + "Repository;\n");

            // Initialize PasswordEncoder for User entity
            if (isUserEntity) {
                writer.write("        this.passwordEncoder = passwordEncoder;\n");
            }

            writer.write("    }\n\n");

            // Get All Method
            writer.write("    public List<" + entityName + "> getAll" + entityName + "s() {\n");
            writer.write("        return " + entityName.toLowerCase() + "Repository.findAll();\n");
            writer.write("    }\n\n");

            // Create Method
            writer.write("    public " + entityName + " create" + entityName + "(" + entityName + " " + entityName.toLowerCase() + ") {\n");

            // Hash password for User entity
            if (isUserEntity) {
                writer.write("        " + entityName.toLowerCase() + ".setPassword(passwordEncoder.encode(" + entityName.toLowerCase() + ".getPassword()));\n");
            }

            writer.write("        return " + entityName.toLowerCase() + "Repository.save(" + entityName.toLowerCase() + ");\n");
            writer.write("    }\n\n");

            // Get by ID Method
            writer.write("    public Optional<" + entityName + "> get" + entityName + "ById(Long id) {\n");
            writer.write("        return " + entityName.toLowerCase() + "Repository.findById(id);\n");
            writer.write("    }\n\n");

            // Update Method
            writer.write("    public " + entityName + " update" + entityName + "(Long id, " + entityName + " " + entityName.toLowerCase() + "Details) {\n");
            writer.write("        Optional<" + entityName + "> optional" + entityName + " = get" + entityName + "ById(id);\n");
            writer.write("        if (optional" + entityName + ".isPresent()) {\n");
            writer.write("            " + entityName + " " + entityName.toLowerCase() + " = optional" + entityName + ".get();\n");
            for (AttributeDTO attribute : attributes) {
                String attrName = attribute.getAttributeName();
                if (isUserEntity && attrName.equalsIgnoreCase("password")) {
                    writer.write("            if (" + entityName.toLowerCase() + "Details.getPassword() != null && !" + entityName.toLowerCase() + "Details.getPassword().isEmpty()) {\n");
                    writer.write("                " + entityName.toLowerCase() + ".setPassword(passwordEncoder.encode(" + entityName.toLowerCase() + "Details.getPassword()));\n");
                    writer.write("            }\n");
                } else {
                    writer.write("            " + entityName.toLowerCase() + ".set" + capitalizeFirstLetter(attrName) + "(" + entityName.toLowerCase() + "Details.get" + capitalizeFirstLetter(attrName) + "());\n");
                }
            }
            writer.write("            return " + entityName.toLowerCase() + "Repository.save(" + entityName.toLowerCase() + ");\n");
            writer.write("        }\n");
            writer.write("        throw new EntityNotFoundException(\"Entity not found\");\n");
            writer.write("    }\n\n");

            // Delete Method
            writer.write("    public void delete" + entityName + "(Long id) {\n");
            writer.write("        " + entityName.toLowerCase() + "Repository.deleteById(id);\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    public static void generateSpringBehaviorServiceClass(
            String projectName,
            String entityName,
            String behaviorMethods) throws IOException {

        String className = entityName + "BehaviorService";
        String packagePath = projectName.toLowerCase();
        String basePackage = "com.example." + projectName.toLowerCase();
        String cleanedBehaviorMethods = Arrays.stream(behaviorMethods.split("\\r?\\n"))
                .filter(line -> !line.startsWith("Here are the method implementations for ") &&
                        !line.startsWith("Here are the method implementations:") &&
                        !line.startsWith("Here are 5 business logic methods"))
                .collect(Collectors.joining("\n"));

        String classCode = """
                package %s.behaviorservices;
                
                import %s.models.*;
                import %s.repositories.*;
                import lombok.AllArgsConstructor;
                import org.springframework.stereotype.Service;
                import jakarta.transaction.Transactional;
                import java.time.*;
                import java.math.*;
                import java.util.stream.Collectors;
                
                import java.util.*;
                
                @Service
                @AllArgsConstructor
                public class %s {
                
                    %s
                }
                """.formatted(
                basePackage,
                basePackage,
                basePackage,
                className,
                cleanedBehaviorMethods
        );

        String baseDir = "./" + projectName + "/src/main/java/com/example/" + packagePath + "/behaviorservices";
        String path = baseDir + "/" + className + ".java";
        Files.write(Paths.get(path), classCode.getBytes());
    }

    public static void generateAuthenticationServiceClass(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/services";
        String className = "AuthenticationService";
        String classContent = String.format("""
                package com.example.%s.services;
                
                import com.example.%s.models.User;
                import com.example.%s.config.jwt.JwtService;
                import com.example.%s.security.SecurityUser;
                import com.example.%s.config.util.CookieUtil;
                import jakarta.servlet.http.HttpServletResponse;
                import org.springframework.beans.factory.annotation.Value;
                import org.springframework.security.authentication.AuthenticationManager;
                import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
                import org.springframework.security.core.Authentication;
                import org.springframework.stereotype.Service;
                import org.springframework.transaction.annotation.Transactional;
                
                @Service
                @Transactional
                public class AuthenticationService {
                
                    private final AuthenticationManager authenticationManager;
                    private final UserService userService;
                    private final JwtService jwtService;
                
                    @Value("${JWT_EXPIRATION_TIME}")
                    private int accessTokenExpiration;
                
                    @Value("${JWT_REFRESH_EXPIRATION_TIME}")
                    private int refreshTokenExpiration;
                
                    public AuthenticationService(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
                         this.authenticationManager = authenticationManager;
                         this.userService = userService;
                         this.jwtService = jwtService;
                    }
                
                    public void registerUser(User user) {
                         userService.createUser(user);
                    }
                
                    public void loginUser(User user, HttpServletResponse response) {
                        Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
                        );
                
                        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
                        User authenticatedUser = securityUser.user();
                
                        String accessToken = jwtService.generateToken(authenticatedUser);
                        String refreshToken = jwtService.generateRefreshToken(authenticatedUser);
                
                        CookieUtil.addCookie("JWT", accessToken, (accessTokenExpiration / 1000), response);
                        CookieUtil.addCookie("Refresh_Token", refreshToken, (refreshTokenExpiration / 1000), response);
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }
}
