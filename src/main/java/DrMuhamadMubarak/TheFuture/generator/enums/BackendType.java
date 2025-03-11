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
}