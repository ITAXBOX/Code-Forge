package DrMuhamadMubarak.TheFuture.generator.enums;

public enum DatabaseType {
    MYSQL, POSTGRESQL, MONGODB, SQLITE;

    public static boolean isValid(String type) {
        for (DatabaseType databaseType : DatabaseType.values()) {
            if (databaseType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a string input to the corresponding DatabaseType enum value.
     * Handles common alternative formats like those used in the start.html form.
     *
     * @param input the string representation to convert
     * @return the matching DatabaseType enum value, or null if no match is found
     */
    public static DatabaseType fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String normalized = input.replaceAll("[ _-]", "").toUpperCase();

        switch (normalized) {
            case "MYSQL":
            case "MARIADB":
                return MYSQL;
            case "POSTGRESQL":
            case "POSTGRES":
                return POSTGRESQL;
            case "MONGODB":
            case "MONGO":
                return MONGODB;
            case "SQLITE":
            case "SQLITE3":
                return SQLITE;
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