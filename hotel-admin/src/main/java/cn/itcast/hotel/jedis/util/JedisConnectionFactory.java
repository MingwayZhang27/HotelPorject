package cn.itcast.hotel.jedis.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisConnectionFactory {
    private static final JedisPool jedisPool ;

    static {
        JedisPoolConfig poolConfig=new JedisPoolConfig();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMaxIdle(0);
        poolConfig.setMaxWaitMillis(1000);

        //创建连接词对象
        jedisPool= new JedisPool(poolConfig,"192.168.32.133",
                6379,100);
    }

    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
}
