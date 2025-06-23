package DrMuhamadMubarak.TheFuture.generator.enums;

public enum BackendType {
    SPRING_BOOT, DJANGO, EXPRESS, FLASK;

    public static boolean isValid(String type) {
        for (BackendType backendType : BackendType.values()) {
            if (backendType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a string input to the corresponding BackendType enum value.
     * Handles common alternative formats like those used in the start.html form.
     *
     * @param input the string representation to convert
     * @return the matching BackendType enum value, or null if no match is found
     */
    public static BackendType fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String normalized = input.replaceAll("[ _-]", "").toUpperCase();

        switch (normalized) {
            case "SPRINGBOOT":
            case "SPRING":
                return SPRING_BOOT;
            case "DJANGO":
            case "PYTHON":
                return DJANGO;
            case "EXPRESS":
            case "EXPRESSJS":
            case "NODEJS":
            case "NODE":
                return EXPRESS;
            case "FLASK":
                return FLASK;
            default:
                // Try a direct match as fallback
                try {
                    return valueOf(input.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return null;
                }
        }
    }
}