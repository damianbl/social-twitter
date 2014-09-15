package eu.softelo.infrastructure.caching;

import net.spy.memcached.AddrUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by dabl on 2014-09-15.
 */

@Service
public class DefaultMemcachedClient implements MemcachedClient {
    private net.spy.memcached.MemcachedClient memcachedClient;

    @Value("${memcached.host}")
    private String host;

    @Value("${memcached.port}")
    private String port;

    @PostConstruct
    public void postConstruct() throws IOException {
        memcachedClient = new net.spy.memcached.MemcachedClient(AddrUtil.getAddresses(host + ":" + port));
    }

    @Override
    public void set(String key, int expirationTime, Object value) {
        memcachedClient.set(key, expirationTime, value);
    }

    @Override
    public Object get(String key) {
        return memcachedClient.get(key);
    }
}
