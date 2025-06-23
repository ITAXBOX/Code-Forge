package DrMuhamadMubarak.TheFuture.generator.enums;

public enum FrontendType {
    NEXT_JS, REACT, VUE, ANGULAR;

    public static boolean isValid(String type) {
        for (FrontendType frontendType : FrontendType.values()) {
            if (frontendType.name().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a string input to the corresponding FrontendType enum value.
     * Handles common alternative formats like those used in the start.html form.
     *
     * @param input the string representation to convert
     * @return the matching FrontendType enum value, or null if no match is found
     */
    public static FrontendType fromString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        String normalized = input.replaceAll("[ _-]", "").toUpperCase();

        switch (normalized) {
            case "NEXTJS":
            case "NEXT":
                return NEXT_JS;
            case "REACTJS":
            case "REACT":
                return REACT;
            case "VUEJS":
            case "VUE":
                return VUE;
            case "ANGULARJS":
            case "ANGULAR":
                return ANGULAR;
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