package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpringDataInitializer {

    public static void generateDataInitializerClass(String projectName) {
        try {
            String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/init";
            String className = "DataInitializer";
            String classContent = String.format("""
                    package com.example.%s.init;
                    
                    import org.springframework.boot.CommandLineRunner;
                    import org.springframework.stereotype.Component;
                    import com.example.%s.models.User;
                    import com.example.%s.repositories.UserRepository;
                    import com.example.%s.models.Role;
                    import com.example.%s.repositories.RoleRepository;
                    import org.springframework.security.crypto.password.PasswordEncoder;
                    import java.util.Set;
                    
                    @Component
                    public class DataInitializer implements CommandLineRunner {
                    
                        private final RoleRepository roleRepository;
                        private final UserRepository userRepository;
                        private final PasswordEncoder passwordEncoder;
                    
                        public DataInitializer(RoleRepository roleRepository,
                                              UserRepository userRepository,
                                              PasswordEncoder passwordEncoder) {
                            this.roleRepository = roleRepository;
                            this.userRepository = userRepository;
                            this.passwordEncoder = passwordEncoder;
                        }
                    
                        @Override
                        public void run(String... args) throws Exception {
                            // Initialize roles
                            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));
                    
                            roleRepository.findByName("ROLE_USER")
                                            .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));
                    
                            // Initialize admin user
                            if (!userRepository.existsByUsername("admin")) {
                                User admin = new User();
                                admin.setUsername("admin");
                                admin.setPassword(passwordEncoder.encode("admin"));
                                admin.setEmail("admin@example.com");
                                admin.setRoles(Set.of(adminRole));
                                userRepository.save(admin);
                            }
                        }
                    }
                    """, projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase());

            Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
        } catch (IOException e) {
            System.err.println("Error generating DataInitializer class: " + e.getMessage());
        }
    }
}