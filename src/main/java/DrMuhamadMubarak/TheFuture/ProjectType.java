package DrMuhamadMubarak.TheFuture;

public enum ProjectType {
    THYMELEAF, REACT, VUE, ANGULAR,
    SPRING_BOOT, DJANGO, EXPRESS, FLASK,
    MYSQL, POSTGRESQL, MONGODB, SQLITE;

    public static boolean isValidFrontendType(String type) {
        return type.equalsIgnoreCase(THYMELEAF.name()) ||
               type.equalsIgnoreCase(REACT.name()) ||
               type.equalsIgnoreCase(VUE.name()) ||
               type.equalsIgnoreCase(ANGULAR.name());
    }

    public static boolean isValidBackendType(String type) {
        return type.equalsIgnoreCase(SPRING_BOOT.name()) ||
               type.equalsIgnoreCase(DJANGO.name()) ||
               type.equalsIgnoreCase(EXPRESS.name()) ||
               type.equalsIgnoreCase(FLASK.name());
    }

    public static boolean isValidDatabaseType(String type) {
        return type.equalsIgnoreCase(MYSQL.name()) ||
               type.equalsIgnoreCase(POSTGRESQL.name()) ||
               type.equalsIgnoreCase(MONGODB.name()) ||
               type.equalsIgnoreCase(SQLITE.name());
    }
}