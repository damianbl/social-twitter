package eu.softelo.infrastructure.caching;

/**
 * Created by dabl on 2014-09-15.
 */
public interface MemcachedClient {
    void set(String key, int expirationTime, Object value);

    Object get(String key);
}
