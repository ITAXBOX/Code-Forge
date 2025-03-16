package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SpringUtils {
    public static void generateUtils(String projectName) throws IOException {
        String baseDir = "./" + projectName + "/src/main/java/com/example/" + projectName.toLowerCase() + "/config/util";
        String className = "CookieUtil";
        String classContent = String.format("""
                package com.example.%s.config.util;
                
                import jakarta.servlet.http.Cookie;
                import jakarta.servlet.http.HttpServletResponse;
                
                public class CookieUtil {
                
                    public static void clearCookie(String cookieName, HttpServletResponse response) {
                        Cookie cookie = new Cookie(cookieName, null);
                        cookie.setHttpOnly(true);
                        cookie.setSecure(true);
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                
                    public static void addCookie(String name, String value, int maxAge, HttpServletResponse response) {
                        Cookie cookie = new Cookie(name, value);
                        cookie.setHttpOnly(true);
                        cookie.setSecure(true);
                        cookie.setPath("/");
                        cookie.setMaxAge(maxAge);
                        response.addCookie(cookie);
                    }
                }
                """, projectName.toLowerCase());

        Files.createDirectories(Paths.get(baseDir));
        Files.write(Paths.get(baseDir + "/" + className + ".java"), classContent.getBytes());
    }
}
