package app.base.services.config;

import app.base.utils.ObjectUtils;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConfigMap extends HashMap<String, String> {

    public String getString(String key) {
        return get(key);
    }

    public Long getLong(String key) {
        return ObjectUtils.parseLongOrNull(getString(key));
    }

    public Integer getInteger(String key) {
        return ObjectUtils.parseIntegerOrNull(getString(key));
    }

    public Double getDouble(String key) {
        return ObjectUtils.parseDoubleOrNull(getString(key));
    }

    public Boolean getBoolean(String key) {
        return ObjectUtils.parseBooleanOrNull(getString(key));
    }
}
