package app.base.services.config;

import app.base.RedisService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigCache {

    private final Cache<String, ConfigMap> caffeine = Caffeine.newBuilder().build();
    private final RedisService redis;

    @Value("${info.app.name:-}.system")
    private String key;
    private String field = "configs";

    /*
        Configs
     */
    public void putConfig(String code, String value) {
        if (code == null || value == null) return;

        ConfigMap map = (BooleanUtils.isTrue(redis.hexists(key, field)))
                ? redis.hget(key, field, ConfigMap.class)
                : caffeine.getIfPresent(field);

        if (map != null) map.put(code, value);
        else map = ConfigMap.builder().build();

        if (redis.isAvailable()) redis.hseto(key, field, map);
        else caffeine.put(field, map);
    }

    public void clearConfigs() {
        if (redis.isAvailable()) redis.hdel(key, field);
        else caffeine.invalidate(field);
    }
}
