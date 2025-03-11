package DrMuhamadMubarak.TheFuture.generator.enums;

public enum FrontendType {
    THYMELEAF, REACT, VUE, ANGULAR;

    public static boolean isValid(String type) {
        for (FrontendType frontendType : FrontendType.values()) {
            if (frontendType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}