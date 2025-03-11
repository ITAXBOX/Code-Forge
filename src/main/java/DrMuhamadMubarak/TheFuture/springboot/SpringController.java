package DrMuhamadMubarak.TheFuture.springboot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpringController {
    public static void generateControllerClass(String projectName, String entityName) throws IOException {
        String className = entityName + "Controller";
        String fileName = projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/controllers/" + className + ".java";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write("package com.example." + projectName.toLowerCase() + ".controllers;\n\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".models." + entityName + ";\n");
            writer.write("import com.example." + projectName.toLowerCase() + ".services." + entityName + "Service;\n");
            writer.write("import org.springframework.http.ResponseEntity;\n");
            writer.write("import jakarta.persistence.EntityNotFoundException;\n");
            writer.write("import org.springframework.web.bind.annotation.*;\n\n");
            writer.write("import java.util.List;\n");
            writer.write("import java.util.Optional;\n\n");

            writer.write("@RestController\n");
            writer.write("@RequestMapping(\"/api/" + entityName.toLowerCase() + "s\")\n");
            writer.write("public class " + className + " {\n\n");

            // Constructor-based Dependency Injection
            writer.write("    private final " + entityName + "Service " + entityName.toLowerCase() + "Service;\n\n");

            writer.write("    public " + className + "(" + entityName + "Service " + entityName.toLowerCase() + "Service) {\n");
            writer.write("        this." + entityName.toLowerCase() + "Service = " + entityName.toLowerCase() + "Service;\n");
            writer.write("    }\n\n");

            // Get All Method
            writer.write("    @GetMapping\n");
            writer.write("    public List<" + entityName + "> getAll" + entityName + "s() {\n");
            writer.write("        return " + entityName.toLowerCase() + "Service.getAll" + entityName + "s();\n");
            writer.write("    }\n\n");

            // Create Method
            writer.write("    @PostMapping\n");
            writer.write("    public " + entityName + " create" + entityName + "(@RequestBody " + entityName + " " + entityName.toLowerCase() + ") {\n");
            writer.write("        return " + entityName.toLowerCase() + "Service.create" + entityName + "(" + entityName.toLowerCase() + ");\n");
            writer.write("    }\n\n");

            // Get by ID Method
            writer.write("    @GetMapping(\"/{id}\")\n");
            writer.write("    public ResponseEntity<" + entityName + "> get" + entityName + "ById(@PathVariable Long id) {\n");
            writer.write("        Optional<" + entityName + "> optional" + entityName + " = " + entityName.toLowerCase() + "Service.get" + entityName + "ById(id);\n");
            writer.write("        return optional" + entityName + ".map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());\n");
            writer.write("    }\n\n");

            // Update Method
            writer.write("    @PutMapping(\"/{id}\")\n");
            writer.write("    public ResponseEntity<" + entityName + "> update" + entityName + "(@PathVariable Long id, @RequestBody " + entityName + " " + entityName.toLowerCase() + "Details) {\n");
            writer.write("        try {\n");
            writer.write("            " + entityName + " updated" + entityName + " = " + entityName.toLowerCase() + "Service.update" + entityName + "(id, " + entityName.toLowerCase() + "Details);\n");
            writer.write("            return ResponseEntity.ok(updated" + entityName + ");\n");
            writer.write("        } catch (EntityNotFoundException e) {\n");
            writer.write("            return ResponseEntity.notFound().build();\n");
            writer.write("        }\n");
            writer.write("    }\n\n");

            // Delete Method
            writer.write("    @DeleteMapping(\"/{id}\")\n");
            writer.write("    public ResponseEntity<Void> delete" + entityName + "(@PathVariable Long id) {\n");
            writer.write("        " + entityName.toLowerCase() + "Service.delete" + entityName + "(id);\n");
            writer.write("        return ResponseEntity.noContent().build();\n");
            writer.write("    }\n");

            writer.write("}\n");
        }
    }

    public static void generateAuthenticationControllerClass(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/controllers";
        String className = "AuthenticationController";
        String classContent = String.format("""
                package com.example.%s.controllers;
                
                import com.example.%s.models.User;
                import com.example.%s.repositories.UserRepository;
                import com.example.%s.models.Role;
                import com.example.%s.repositories.RoleRepository;
                import org.springframework.security.crypto.password.PasswordEncoder;
                import org.springframework.web.bind.annotation.*;
                import org.springframework.http.ResponseEntity;
                import org.springframework.security.authentication.AuthenticationManager;
                import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
                import org.springframework.security.core.Authentication;
                import org.springframework.security.core.context.SecurityContextHolder;
                import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
                import jakarta.servlet.http.HttpServletRequest;
                import jakarta.servlet.http.HttpServletResponse;
                import java.util.Collections;
                
                @RestController
                @RequestMapping("/api/auth")
                public class AuthenticationController {
                
                    private final UserRepository userRepository;
                    private final RoleRepository roleRepository;
                    private final PasswordEncoder passwordEncoder;
                    private final AuthenticationManager authenticationManager;
                
                    public AuthenticationController(UserRepository userRepository,
                                                    RoleRepository roleRepository,
                                                    PasswordEncoder passwordEncoder,
                                                    AuthenticationManager authenticationManager) {
                        this.userRepository = userRepository;
                        this.roleRepository = roleRepository;
                        this.passwordEncoder = passwordEncoder;
                        this.authenticationManager = authenticationManager;
                    }
                
                    @PostMapping("/register")
                    public ResponseEntity<String> registerUser(@RequestBody User user) {
                        if (userRepository.existsByUsername(user.getUsername())) {
                            return ResponseEntity.badRequest().body("Username is already taken!");
                        }
                
                        // Assign USER role by default
                        Role userRole = roleRepository.findByName("USER")
                                .orElseThrow(() -> new RuntimeException("Role USER not found"));
                        user.setRoles(Collections.singleton(userRole));
                
                        // Encode the password
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                
                        // Save the user
                        userRepository.save(user);
                
                        return ResponseEntity.ok("User registered successfully!");
                    }
                
                    @PostMapping("/login")
                    public ResponseEntity<String> loginUser(@RequestBody User user) {
                        try {
                            Authentication authentication = authenticationManager.authenticate(
                                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
                            );
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            return ResponseEntity.ok("User logged in successfully!");
                        } catch (Exception e) {
                            return ResponseEntity.status(401).body("Invalid username or password");
                        }
                    }
                
                    @PostMapping("/logout")
                    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
                        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                        if (authentication != null) {
                            new SecurityContextLogoutHandler().logout(request, response, authentication);
                        }
                        return ResponseEntity.ok("User logged out successfully!");
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase());

        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }
}