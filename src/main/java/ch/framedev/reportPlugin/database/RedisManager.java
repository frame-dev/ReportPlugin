package ch.framedev.reportPlugin.database;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Optional;

public class RedisManager {

    private final JedisPool pool;
    private final Gson gson = new Gson();

    public RedisManager(String host, int port, String password) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(10);
        if (password == null || password.isEmpty()) {
            this.pool = new JedisPool(config, host, port);
        } else {
            this.pool = new JedisPool(config, host, port, 2000, password);
        }
    }

    public Optional<String> get(String key) {
        try (Jedis jedis = pool.getResource()) {
            String val = jedis.get(key);
            return Optional.ofNullable(val);
        } catch (Exception e) {
            // log if needed
            return Optional.empty();
        }
    }

    public boolean set(String key, String value, int ttlSeconds) {
        try (Jedis jedis = pool.getResource()) {
            if (ttlSeconds > 0) {
                jedis.setex(key, ttlSeconds, value);
            } else {
                jedis.set(key, value);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public <T> Optional<T> getObject(String key, Class<T> clazz) {
        return get(key).map(json -> gson.fromJson(json, clazz));
    }

    public boolean setObject(String key, Object obj, int ttlSeconds) {
        String json = gson.toJson(obj);
        return set(key, json, ttlSeconds);
    }

    public void del(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        } catch (Exception ignored) {}
    }

    public void close() {
        pool.close();
    }
}
