package app.base.db;

import app.base.RedisService;
import app.base.constants.GErrors;
import app.base.exceptions.GAlreadyExists;
import app.base.exceptions.GNotAllowed;
import app.base.exceptions.GNotFound;
import app.base.objects.IGEnum;
import app.base.utils.ObjectUtils;
import app.base.utils.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GRedisCache<ID extends Serializable, Entity extends GEntity<ID>, Repository extends JpaRepository<Entity, ID>> {

    private final Repository repository;

    private final Class<Entity> clazz;

    public Integer seconds = 1000;

    @Value("${info.app.name}")
    private String system;

    @Autowired
    private RedisService redisService;

    public GRedisCache(Repository repository, Class<Entity> clazz) {
        this.repository = repository;
        this.clazz = clazz;
    }


    /*
        GETTERS AND SETTERS
     */

    public Class<Entity> getClazz() {
        return clazz;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getKey() {
        return system + "." + clazz.getSimpleName();
    }

    public String getKeyAll() {
        return getKey() + ".all";
    }


    /*
        ACTIONS
     */

    /**
     * Override it to modify entity data
     *
     * @param entity Entity
     * @return entity
     */
    protected Entity wrapper(Entity entity) {
        return entity;
    }

    public Entity put(Entity entity) {
        if (entity == null || entity.getCacheKey() == null) return entity;
        redisService.hset(getKey(), entity.getCacheKey(), entity.toJson(), seconds);
        return wrapper(entity);
    }

    public List<Entity> put(List<Entity> entities) {
        if (entities == null) return Collections.emptyList();

        return entities.stream().map(this::put).collect(Collectors.toList());
    }

    public Entity save(Entity entity) {
        if (entity == null) return null;
        return put(repository.save(entity));
    }

    public List<Entity> save(List<Entity> entities) {
        if (entities == null) return Collections.emptyList();
        return put(repository.save(entities));
    }

    private Entity get(ID id) {
        if (ObjectUtils.toStringOrNull(id) == null) return null;
        if (!Boolean.TRUE.equals(redisService.hexists(getKey(), id.toString()))) return put(repository.findOne(id));

        return wrapper(redisService.hget(getKey(), id.toString(), clazz, seconds));
    }


    /**
     * Get all values from cache only
     *
     * @return List of entity
     */
    public List<Entity> get() {
        Map<String, String> map = redisService.hgetMap(getKey());
        return map.values().stream().map(value -> wrapper(SerializationUtils.fromJson(value, clazz))).collect(Collectors.toList());
    }

    public List<Entity> get(List<ID> ids) {
        if (ids == null) return Collections.emptyList();

        List<Entity> values = new ArrayList<>();
        ids.forEach(id -> {
            String key = ObjectUtils.toStringOrNull(id);
            if (key != null) {
                if (!Boolean.TRUE.equals(redisService.exists(key))) {
                    values.add(put(repository.getOne(id)));
                } else {
                    values.add(wrapper(redisService.get(key, clazz, seconds)));
                }
            }
        });

        return values;
    }


    /**
     * <p>WARNING!!!</p>
     * <p>USE CAREFUL!!!</p>
     * <p>DO NOT USE FOR OPERATIONS DATA (sessions, clients, etc.)</p>
     * <p>LIMITED DATA ONLY (catalogs, dictionary etc.)</p>
     *
     * <p>
     * If <code>allCached=false</code> or cached values is empty
     * select from db all values and set to redis
     * </p>
     *
     * @return List of entity
     */
    public List<Entity> getAll() {
        List<Entity> all = get();

        String allKey = getKeyAll();
        if (Boolean.TRUE.equals(redisService.get(allKey, Boolean.class)) && !ObjectUtils.isNull(all) && !all.isEmpty())
            return all;

        all = repository.findAll();
        redisService.seto(allKey, Boolean.TRUE);
        return put(all);
    }

    public void clear() {
        redisService.del(getKey(), getKeyAll());
    }

    public void clear(ID id) {
        if (id == null) return;
        redisService.del(getKeyAll());
        redisService.hdel(getKey(), id.toString());
    }

    public void clear(List<ID> ids) {
        if (ids == null) return;
        redisService.del(getKeyAll());
        redisService.hdel(getKey(), ids.stream().map(ObjectUtils::toStringOrNull).filter(Objects::nonNull).toArray(String[]::new));
    }

    public IGEnum getAlreadyExistsError() {
        return GErrors.ALREADY_EXISTS;
    }

    public IGEnum getNotFoundError() {
        return GErrors.NOT_FOUND;
    }

    public IGEnum getDisabledError() {
        return GErrors.NOT_ALLOWED;
    }

    public void checkIfNotExists(ID id) throws GAlreadyExists {
        if (get(id) != null) throw new GAlreadyExists(getAlreadyExistsError());
    }

    public Entity checkIfExists(Entity entity) throws GNotFound {
        if (entity == null) throw new GNotFound(getNotFoundError());
        return entity;
    }

    public void checkNotDisabled(Entity entity) throws GNotAllowed {
        if (entity.isDisabled()) throw new GNotAllowed(getDisabledError());
    }

    public Entity check(Entity entity) throws GNotAllowed, GNotFound {
        checkIfExists(entity);
        checkNotDisabled(entity);
        return entity;
    }

    public Entity find(ID id) throws GNotAllowed, GNotFound {
        return check(get(id));
    }

    public Entity findDisabled(ID id) throws GNotFound {
        return checkIfExists(get(id));
    }

    public Entity findNotNull(ID id) throws GNotFound {
        Entity entity = get(id);
        checkIfExists(entity);
        return entity;
    }

    public void delete(ID id) throws GNotFound {
        if (!repository.exists(id)) throw new GNotFound(getNotFoundError());

        repository.delete(id);
        clear(id);
    }
}
