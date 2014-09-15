package eu.softelo.infrastructure.caching;

import com.google.common.base.Optional;

/**
 * Created by dabl on 2014-09-15.
 */
public interface CacheService {
    void put(String key, Object value);

    void putWithExpirationTime(String key, int time, Object value);

    Optional<Object> get(String key);
}
