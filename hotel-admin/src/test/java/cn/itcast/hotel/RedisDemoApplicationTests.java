package cn.itcast.hotel;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisDemoApplicationTests {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    void testString(){
        //写入一条String数据
        redisTemplate.opsForValue().set("name","苗苗");
        //获取String数据
        Object name = redisTemplate.opsForValue().get("name");
        System.out.println(name);
    }
}
