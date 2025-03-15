package DrMuhamadMubarak.TheFuture.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
}
