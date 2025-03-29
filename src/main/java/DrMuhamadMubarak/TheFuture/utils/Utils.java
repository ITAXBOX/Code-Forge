package DrMuhamadMubarak.TheFuture.utils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

public class Utils {
    public static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static void createDirectory(String path) throws IOException {
        Files.createDirectories(Paths.get(path));
    }

    public static String generateSecureToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32]; // 32 bytes = 256 bits
        secureRandom.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }

    public static void deleteProjectDirectory(String projectName) {
        String BASE_PATH = "D:\\C.S\\Project Generator\\";

        if (projectName == null || projectName.trim().isEmpty()) {
            System.out.println("No project name provided for deletion");
            return;
        }

        Path projectPath = Paths.get(BASE_PATH + projectName).normalize().toAbsolutePath();

        // Verify the path is within our base directory
        Path basePath = Paths.get(BASE_PATH).normalize().toAbsolutePath();
        if (!projectPath.startsWith(basePath)) {
            System.err.println("Security violation: Attempted to delete outside base directory: " + projectPath);
            return;
        }

        try {
            if (Files.exists(projectPath)) {
                deleteRecursively(projectPath);
                System.out.println("Successfully deleted project: " + projectPath);
            } else {
                System.out.println("Project directory doesn't exist: " + projectPath);
            }
        } catch (IOException e) {
            System.err.println("Error deleting project: " + projectPath);
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                for (Path entry : stream) {
                    deleteRecursively(entry);
                }
            }
        }
        Files.delete(path);
        System.out.println("Deleted: " + path);
    }
}
