package backend.academy.configuration;

public enum EnvType {
    DEVELOPMENT, PRODUCTION, TEST, UNKNOWN;

    public static EnvType getFromType(String environment) {
        return switch (environment) {
            case "dev" -> DEVELOPMENT;
            case "prod" -> PRODUCTION;
            case "test" -> TEST;
            default -> UNKNOWN;
        };
    }
}
