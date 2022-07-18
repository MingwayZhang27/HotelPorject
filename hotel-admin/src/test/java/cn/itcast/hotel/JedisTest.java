package cn.itcast.hotel;

import cn.itcast.hotel.jedis.util.JedisConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

public class JedisTest {
    private Jedis jedis;

    @BeforeEach
    void setUp(){
        //1、连接建立
//        jedis=new Jedis("192.168.32.133",6379);
        jedis= JedisConnectionFactory.getJedis();

        //2、设置密码
//        jedis.auth("123321");
        //3.选择库
        jedis.select(0);
    }

    @Test
    void testString(){
        //存入数据
        String result = jedis.set("name", "苗苗");
        System.out.println("result="+result);
        //获取数据
        String name = jedis.get("name");
        System.out.println("name="+name);
    }

    @AfterEach
    void tearDown(){
        if(jedis!=null){
            jedis.close();
        }
    }
}
