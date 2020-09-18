package app.base.objects;

public interface IGConfig {

    String name();

    String getKey();

    String getValue();

    String getDescription();

    default Class getClazz() {
        return String.class;
    }

    default String getDefaultKey() {
        return name().toLowerCase().replace("_", ".");
    }

}
