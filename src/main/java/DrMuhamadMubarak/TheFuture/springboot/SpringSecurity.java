package DrMuhamadMubarak.TheFuture.springboot;

import DrMuhamadMubarak.TheFuture.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpringSecurity {
    public static void generateSecurityClass(String projectName) {
        try {
            generateSecurityConfigClass(projectName);
            generateUserDetailsService(projectName);

            generateSecurityUser(projectName);
            generateSecurityRole(projectName);

            generateJwtAuthFilter(projectName);
            generateJwtService(projectName);

            generateEnvFile(projectName, Utils.generateSecureToken());
        } catch (IOException e) {
            System.err.println("Error generating security classes: " + e.getMessage());
        }
    }

    private static void generateSecurityConfigClass(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/config";
        String className = "SecurityConfig";
        String classContent = String.format("""
                package com.example.%s.config;
                
                import com.example.%s.config.jwt.JwtAuthFilter;
                import lombok.AllArgsConstructor;
                import org.springframework.context.annotation.Bean;
                import org.springframework.context.annotation.Configuration;
                import org.springframework.security.authentication.AuthenticationManager;
                import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
                import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
                import org.springframework.security.config.annotation.web.builders.HttpSecurity;
                import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
                import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
                import org.springframework.security.config.http.SessionCreationPolicy;
                import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
                import org.springframework.security.crypto.password.PasswordEncoder;
                import org.springframework.security.web.SecurityFilterChain;
                import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
                
                @Configuration
                @EnableWebSecurity
                @EnableMethodSecurity
                @AllArgsConstructor
                public class SecurityConfig {
                
                    private final JwtAuthFilter jwtAuthenticationFilter;
                
                    @Bean
                    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                        http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                                        .anyRequest().authenticated()
                                )
                                .sessionManagement(sess -> sess
                                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                                )
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                
                        return http.build();
                    }
                
                    @Bean
                    public PasswordEncoder passwordEncoder() {
                        return new BCryptPasswordEncoder();
                    }
                
                    @Bean
                    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
                        return config.getAuthenticationManager();
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateUserDetailsService(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/security";
        String className = "CustomUserDetailsService";
        String classContent = String.format("""
                package com.example.%s.security;
                
                import com.example.%s.models.User;
                import com.example.%s.repositories.UserRepository;
                import org.springframework.security.core.userdetails.UserDetails;
                import org.springframework.security.core.userdetails.UserDetailsService;
                import org.springframework.security.core.userdetails.UsernameNotFoundException;
                import org.springframework.stereotype.Service;
                import lombok.AllArgsConstructor;
                import java.util.Optional;
                
                @Service
                @AllArgsConstructor
                public class CustomUserDetailsService implements UserDetailsService {
                    private final UserRepository userRepository;
                
                    @Override
                    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                        Optional<User> user = userRepository.findByEmail(email);
                
                        return user.map(SecurityUser::new)
                                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateSecurityUser(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/security";
        String className = "SecurityUser";
        String classContent = String.format("""
                package com.example.%s.security;
                
                import com.example.%s.models.User;
                import org.springframework.security.core.GrantedAuthority;
                import org.springframework.security.core.userdetails.UserDetails;
                
                import java.util.Collection;
                import java.util.stream.Collectors;
                
                public record SecurityUser(User user) implements UserDetails {
                    @Override
                    public String getUsername() {
                        return user.getEmail();
                    }
                
                    @Override
                    public String getPassword() {
                        return user.getPassword();
                    }
                
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return user.getRoles() // Assuming User has a getRoles() method
                                .stream()
                                .map(SecurityRole::new)
                                .collect(Collectors.toList());
                    }
                
                    @Override
                    public boolean isAccountNonExpired() {
                        return true;
                    }
                
                    @Override
                    public boolean isAccountNonLocked() {
                        return true;
                    }
                
                    @Override
                    public boolean isCredentialsNonExpired() {
                        return true;
                    }
                
                    @Override
                    public boolean isEnabled() {
                        return true;
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase());

        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateSecurityRole(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/security";
        String className = "SecurityRole";
        String classContent = String.format("""
                package com.example.%s.security;
                
                import com.example.%s.models.Role;
                import lombok.AllArgsConstructor;
                import org.springframework.security.core.GrantedAuthority;
                
                @AllArgsConstructor
                public class SecurityRole implements GrantedAuthority {
                    private final Role role;
                
                    @Override
                    public String getAuthority() {
                        return role.getName();
                    }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateJwtAuthFilter(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/config/jwt";
        String className = "JwtAuthFilter";
        String classContent = String.format("""
                package com.example.%s.config.jwt;
                
                import jakarta.servlet.FilterChain;
                import jakarta.servlet.ServletException;
                import jakarta.servlet.http.Cookie;
                import jakarta.servlet.http.HttpServletRequest;
                import jakarta.servlet.http.HttpServletResponse;
                import lombok.NonNull;
                import org.springframework.beans.factory.annotation.Value;
                import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
                import org.springframework.security.core.context.SecurityContextHolder;
                import org.springframework.security.core.userdetails.UserDetails;
                import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
                import org.springframework.stereotype.Component;
                import org.springframework.web.filter.OncePerRequestFilter;
                import java.io.IOException;
                
                @Component
                public class JwtAuthFilter extends OncePerRequestFilter {
                    @Value("${JWT_EXPIRATION_TIME}")
                    private int accessTokenExpiration;
                
                    private final JwtService jwtService;
                
                    public JwtAuthFilter(JwtService jwtService) {
                        this.jwtService = jwtService;
                    }
                
                    @Override
                    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
                        String token = getTokenFromCookies(request);
                
                        if (token == null) {
                            String authHeader = request.getHeader("Authorization");
                            if (authHeader != null && authHeader.startsWith("Bearer ") && authHeader.length() > 7) {
                                token = authHeader.substring(7);
                            }
                        }
                
                        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            try {
                                if (!jwtService.isTokenExpired(token)) {
                                    UserDetails userDetails = jwtService.getUserDetailsFromToken(token);
                                    authenticateUser(request, userDetails);
                                } else {
                                    String refreshToken = getRefreshTokenFromCookies(request);
                                    if (refreshToken != null && !jwtService.isTokenExpired(refreshToken)) {
                                        UserDetails userDetails = jwtService.getUserDetailsFromToken(refreshToken);
                                        String newAccessToken = jwtService.generateTokenFromUserDetails(userDetails);
                
                                        Cookie newAccessTokenCookie = new Cookie("JWT", newAccessToken);
                                        newAccessTokenCookie.setHttpOnly(true);
                                        newAccessTokenCookie.setSecure(true);
                                        newAccessTokenCookie.setPath("/");
                                        newAccessTokenCookie.setMaxAge(accessTokenExpiration / 1000);
                                        response.addCookie(newAccessTokenCookie);
                
                                        authenticateUser(request, userDetails);
                                    } else {
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Refresh token expired or invalid");
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + e.getMessage());
                                return;
                            }
                        }
                
                        filterChain.doFilter(request, response);
                    }
                
                    private void authenticateUser(HttpServletRequest request, UserDetails userDetails) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                
                    private String getTokenFromCookies(HttpServletRequest request) {
                        Cookie[] cookies = request.getCookies();
                        if (cookies != null) {
                            for (Cookie cookie : cookies) {
                                if ("JWT".equals(cookie.getName())) {
                                    return cookie.getValue();
                                }
                            }
                        }
                        return null;
                    }
                
                    private String getRefreshTokenFromCookies(HttpServletRequest request) {
                        Cookie[] cookies = request.getCookies();
                        if (cookies != null) {
                            for (Cookie cookie : cookies) {
                                if ("Refresh_Token".equals(cookie.getName())) {
                                    return cookie.getValue();
                                }
                            }
                        }
                        return null;
                    }
                }
                """, projectName.toLowerCase());
        Files.createDirectories(Paths.get(baseDir));
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateJwtService(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/config/jwt";
        String className = "JwtService";
        String classContent = String.format("""
                package com.example.%s.config.jwt;
                
                import com.example.%s.models.Role;
                import com.example.%s.security.SecurityUser;
                import com.example.%s.models.User;
                import com.example.%s.repositories.UserRepository;
                import io.jsonwebtoken.Claims;
                import io.jsonwebtoken.Jwts;
                import io.jsonwebtoken.io.Decoders;
                import io.jsonwebtoken.security.Keys;
                import org.springframework.beans.factory.annotation.Value;
                import org.springframework.security.core.userdetails.UserDetails;
                import org.springframework.security.core.userdetails.UsernameNotFoundException;
                import org.springframework.stereotype.Component;
                import javax.crypto.SecretKey;
                import java.util.*;
                import java.util.function.Function;
                import java.util.stream.Collectors;
                
                @Component
                public class JwtService {
                
                    @Value("${JWT_TOKEN_SECRET}")
                    private String SECRET;
                
                    @Value("${JWT_EXPIRATION_TIME}")
                    private long EXPIRATION_TIME;
                
                    @Value("${JWT_REFRESH_EXPIRATION_TIME}")
                    private long REFRESH_EXPIRATION_TIME;
                
                    private final UserRepository userRepository;
                
                    public JwtService(UserRepository userRepository) {
                        this.userRepository = userRepository;
                    }
                
                    public String generateToken(User user) {
                        return createToken(user, EXPIRATION_TIME);
                    }
                
                    public String generateRefreshToken(User user) {
                        return createToken(user, REFRESH_EXPIRATION_TIME);
                    }
                
                    private String createToken(User user, long expirationTime) {
                        Map<String, Object> claims = new HashMap<>();
                        claims.put("id", user.getId());
                        claims.put("email", user.getEmail());
                        claims.put("roles", user.getRoles().stream()
                        .map(Role::getName).collect(Collectors.toList()));
                
                        return Jwts.builder()
                                .subject(user.getEmail())
                                .claims(claims)
                                .issuedAt(new Date(System.currentTimeMillis()))
                                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                                .signWith(getSignKey())
                                .compact();
                    }
                
                    public String generateTokenFromUserDetails(UserDetails userDetails) {
                        String email = userDetails.getUsername();
                
                        User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                
                        return createToken(user, EXPIRATION_TIME);
                    }
                
                    private SecretKey getSignKey() {
                        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
                        return Keys.hmacShaKeyFor(keyBytes);
                    }
                
                    public Date extractExpiration(String token) {
                        return extractClaim(token, Claims::getExpiration);
                    }
                
                    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
                        final Claims claims = extractAllClaims(token);
                        return claimsResolver.apply(claims);
                    }
                
                    private Claims extractAllClaims(String token) {
                        return Jwts.parser()
                                .verifyWith(getSignKey())
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
                    }
                
                    public Boolean isTokenExpired(String token) {
                        return extractExpiration(token).before(new Date());
                    }
                
                    public UserDetails getUserDetailsFromToken(String token) {
                        Claims claims = extractAllClaims(token);
                
                        User user = toUserFromClaims(claims);
                
                        return new SecurityUser(user);
                    }
                
                    public static User toUserFromClaims(Claims claims) {
                            if (claims == null) {
                                return null;
                            }
                
                            User user = new User();
                            user.setId(claims.get("id", long.class));
                            user.setEmail(claims.get("email", String.class));
                
                            List<String> roles = claims.get("roles", List.class);
                            if (roles != null) {
                                Set<Role> roleSet =roles.stream()
                                        .map(Role::new)
                                        .collect(Collectors.toSet());
                                user.setRoles(roleSet);
                            }
                
                            return user;
                        }
                }
                """, projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase(), projectName.toLowerCase());
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }

    private static void generateEnvFile(String projectName, String jwtTokenSecret) throws IOException {
        String baseDir = "./" + projectName + "/src/main/resources";
        String fileName = ".env";
        String envContent = String.format("""
                JWT_TOKEN_SECRET=%s
                JWT_EXPIRATION_TIME=3600000
                JWT_REFRESH_EXPIRATION_TIME=604800000
                """, jwtTokenSecret);
        Files.createDirectories(Paths.get(baseDir));
        Files.write(Paths.get(baseDir + "/" + fileName), envContent.getBytes());
    }
}