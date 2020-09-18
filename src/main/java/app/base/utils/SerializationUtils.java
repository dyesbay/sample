package app.base.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class SerializationUtils {
    private static final Gson GSON = new GsonBuilder()
            .create();

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
