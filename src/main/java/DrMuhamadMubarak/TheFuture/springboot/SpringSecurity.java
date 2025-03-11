package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpringSecurity {
    public static void generateSecurityClass(String projectName) {
        try {
            generateSecurityConfigClass(projectName);
            generateUserDetailsService(projectName);
        } catch (IOException e) {
            System.err.println("Error generating security classes: " + e.getMessage());
        }
    }

    private static void generateSecurityConfigClass(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/config";
        String className = "SecurityConfig";
        String classContent = String.format("""
                package com.example.%s.config;
                
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                import org.springframework.security.config.annotation.web.builders.HttpSecurity;
                import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
                import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
                import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
                import org.springframework.security.crypto.password.PasswordEncoder;
                import org.springframework.security.web.SecurityFilterChain;
                
                @Configuration
                @EnableWebSecurity
                public class SecurityConfig {
                
                    @Bean
                    public PasswordEncoder passwordEncoder() {
                        return new BCryptPasswordEncoder();
                    }
                
                    @Bean
                    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                        http
                            .csrf(AbstractHttpConfigurer::disable)
                            .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/public/**").permitAll()
                                .requestMatchers("/register").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                            )
                            .httpBasic(_ -> {});
                        return http.build();
                    }
                }
                """, projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateUserDetailsService(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/security";
        String className = "CustomUserDetailsService";
        String classContent = String.format("""
                package com.example.%s.security;
                
                import org.springframework.security.core.userdetails.UserDetails;
                import org.springframework.security.core.userdetails.UserDetailsService;
                import org.springframework.security.core.userdetails.UsernameNotFoundException;
                import org.springframework.stereotype.Service;
                import com.example.%s.models.User;
                import com.example.%s.repositories.UserRepository;
                import org.springframework.security.core.authority.SimpleGrantedAuthority;
                import java.util.stream.Collectors;
                
                @Service
                public class CustomUserDetailsService implements UserDetailsService {
                
                    private final UserRepository userRepository;
                
                    public CustomUserDetailsService(UserRepository userRepository) {
                        this.userRepository = userRepository;
                    }
                
                    @Override
                    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                        User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                        return new org.springframework.security.core.userdetails.User(
                            user.getUsername(),
                            user.getPassword(),
                            user.getRoles().stream()
                                .map(role -> new SimpleGrantedAuthority(role.getName()))
                                .collect(Collectors.toList())
                        );
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }
}