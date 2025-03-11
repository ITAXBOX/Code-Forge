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
}