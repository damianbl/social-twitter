package eu.softelo.infrastructure.caching;

import com.google.common.base.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;

/**
 * Created by dabl on 2014-09-15.
 */

@Service
public class MemcachedCacheService implements CacheService {
    private static final int DEFAULT_EXPIRATION_TIME = 900;

    private MemcachedClient memcachedClient;

    @Autowired
    public MemcachedCacheService(MemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    @Override
    public void put(String key, Object value) {
        memcachedClient.set(key, DEFAULT_EXPIRATION_TIME, value);
    }

    @Override
    public void putWithExpirationTime(String key, int expTime, Object value) {
        memcachedClient.set(key, expTime, value);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.fromNullable(memcachedClient.get(key));
    }
}
