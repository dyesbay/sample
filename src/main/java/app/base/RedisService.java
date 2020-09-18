package app.base;

import app.base.utils.ObjectUtils;
import app.base.utils.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RedisService {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JedisPool jedisPoolWrite;
    private JedisPool jedisPoolRead;

    @Value("${info.redis.enabled:true}")
    private Boolean enabled;

    @Value("${info.redis.timeout:3000}")
    private Integer timeout;

    @Value("${info.redis.write.host:app-redis}")
    private String hostWrite;

    @Value("${info.redis.write.port:6379}")
    private Integer portWrite;

    @Value("${info.redis.read.host:app-redis}")
    private String hostRead;

    @Value("${info.redis.read.port:6379}")
    private Integer portRead;


    @PostConstruct
    public void init() {
        this.jedisPoolWrite = new JedisPool(buildPoolConfig(), hostWrite, portWrite, timeout);
        this.jedisPoolRead = new JedisPool(buildPoolConfig(), hostRead, portRead, timeout);
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    private JedisPoolConfig buildPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(256);
        poolConfig.setMaxIdle(256);
        poolConfig.setMinIdle(16);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        return poolConfig;
    }

    public boolean isAvailable() {
        if (!Boolean.TRUE.equals(enabled)) return false;

        try (Jedis jedis = jedisPoolWrite.getResource()) {
            jedis.ping();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public String ping() {
        try (Jedis jedis = jedisPoolWrite.getResource()) {
            return jedis.ping();
        } catch (Exception ex) {
            return "unavailable";
        }
    }

    public void setExpire(String key, Integer seconds) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return;

        try (Jedis jedis = jedisPoolWrite.getResource()) {
            if (seconds != null) jedis.expire(key, seconds);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }


    /*
        Key value
     */

    public void set(String key, String value, Integer seconds) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return;

        try (Jedis jedis = jedisPoolWrite.getResource()) {
            jedis.set(key, value);
            if (seconds != null) jedis.expire(key, seconds);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public void set(String key, String value) {
        set(key, value, null);
    }

    public void seto(String key, Object value) {
        set(key, SerializationUtils.toJson(value), null);
    }

    public void seto(String key, Object value, Integer seconds) {
        set(key, SerializationUtils.toJson(value), seconds);
    }


    public String getex(String key, Integer expired) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return null;

        if (expired != null) {
            try (Jedis jedis = jedisPoolWrite.getResource()) {
                jedis.expire(key, expired);
            } catch (Exception ex) {
                // do nothing
            }
        }

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.get(key);
        } catch (Exception ex) {
            return null;
        }
    }

    public String get(String key) {
        return getex(key, null);
    }

    public <T> T get(String key, Class<T> clazz, Integer expired) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return null;

        return SerializationUtils.fromJson(getex(key, expired), clazz);
    }

    public <T> T get(String key, Class<T> clazz) {
        return get(key, clazz, null);
    }

    public <T> T getOrDefault(String key, Class<T> clazz, T def) {
        T value = get(key, clazz);
        if (value == null) return def;
        return value;
    }

    public Boolean exists(String key) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return false;

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.exists(key);
        } catch (Exception ex) {
            return false;
        }
    }

    public void del(String key) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return;

        try (Jedis jedis = jedisPoolWrite.getResource()) {
            jedis.del(key);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public void del(String... key) {
        for (String s : key) del(s);
    }


    public Set<String> keys(String pattern) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(pattern)) return new HashSet<>();

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.keys(pattern);
        } catch (Exception ex) {
            return new HashSet<>();
        }
    }

    public Set<String> keys() {
        return keys("*");
    }

    /*
        Key HashMap
     */

    public void hset(String key, String field, String value, Integer expired) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key) || ObjectUtils.isBlank(field)) return;

        try (Jedis jedis = jedisPoolWrite.getResource()) {
            jedis.hset(key, field, value);
            if (expired != null) jedis.expire(key, expired);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
    }

    public void hset(String key, String field, String value) {
        hset(key, field, value, null);
    }

    public void hseto(String key, String field, Object value) {
        hset(key, field, SerializationUtils.toJson(value), null);
    }

    public void hseto(String key, String field, Object value, Integer expired) {
        hset(key, field, SerializationUtils.toJson(value), expired);
    }


    public String hgetex(String key, String field, Integer expired) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key) || ObjectUtils.isBlank(field)) return null;

        if (expired != null) {
            try (Jedis jedis = jedisPoolWrite.getResource()) {
                jedis.expire(key, expired);
            } catch (Exception ex) {
                // do nothing
            }
        }

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.hget(key, field);
        } catch (Exception ex) {
            return null;
        }
    }

    public String hget(String key, String field) {
        return hgetex(key, field, null);
    }

    public <T> T hget(String key, String field, Class<T> clazz, Integer expired) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key) || ObjectUtils.isBlank(field)) return null;

        return SerializationUtils.fromJson(hgetex(key, field, expired), clazz);
    }

    public <T> T hget(String key, String field, Class<T> clazz) {
        return hget(key, field, clazz, null);
    }


    public Map<String, String> hgetMap(String key) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return Collections.emptyMap();

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.hgetAll(key);
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    public <T> Map<String, T> hgetMap(String key, Class<T> clazz) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return Collections.emptyMap();

        Map<String, String> map = hgetMap(key);
        Map<String, T> result = new HashMap<>();
        map.keySet().forEach(code -> result.put(code, SerializationUtils.fromJson(map.get(code), clazz)));
        return result;
    }


    public List<String> hgetList(String key) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return Collections.emptyList();

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return new ArrayList<>(jedis.hgetAll(key).values());
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    public <T> List<T> hgetList(String key, Class<T> clazz) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return Collections.emptyList();

        return hgetList(key).stream().map(value -> SerializationUtils.fromJson(value, clazz)).collect(Collectors.toList());
    }


    public Boolean hexists(String key, String field) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return false;

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.hexists(key, field);
        } catch (Exception ex) {
            return false;
        }
    }

    public void hdel(String key, String... fields) {
        if (!Boolean.TRUE.equals(enabled) || Boolean.TRUE.equals(ObjectUtils.isBlank(key)) || fields == null || fields.length == 0)
            return;

        try (Jedis jedis = jedisPoolWrite.getResource()) {
            jedis.hdel(key, fields);
        } catch (Exception ex) {
            logger.error("{}: {} {}", ex.getMessage(), key, fields);
        }
    }

    public Set<String> hkeys(String key) {
        if (!Boolean.TRUE.equals(enabled) || ObjectUtils.isBlank(key)) return new HashSet<>();

        try (Jedis jedis = jedisPoolRead.getResource()) {
            return jedis.hkeys(key);
        } catch (Exception ex) {
            return new HashSet<>();
        }
    }
}
