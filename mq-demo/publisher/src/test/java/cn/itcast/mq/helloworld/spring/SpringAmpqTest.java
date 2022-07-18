package cn.itcast.mq.helloworld.spring;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmpqTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage2SimpleQueue(){
        String queueName="simple.queue";
        String message="hello,spring amqp!";
        rabbitTemplate.convertAndSend(queueName,message);
    }

    @Test
    public void testSendMessage2WrokQueue() throws InterruptedException {
        String queueName="simple.queue";
        String message="hello,message__";
        for(int i=1;i<=50;i++){
            rabbitTemplate.convertAndSend(queueName,message+i);
            Thread.sleep(20);
        }
    }

    @Test
    public void testSendFanoutExchange(){
        //交换机名称
        String exchangeName="itcast.fanout";
        //消息
        String message="hello.everyone!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"",message);
    }

    @Test
    public void testSendDirectExchange(){
        //交换机名称
        String exchangeName="itcast.direct";
        //消息
        String message="hello.red!";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"red",message);
    }

    @Test
    public void testSendTopicExchange(){
        //交换机名称
        String exchangeName="itcast.topic";
        //消息
        String message="牛牛喵喵";
        //发送消息
        rabbitTemplate.convertAndSend(exchangeName,"china.weather",message);
    }

    @Test
    public void testSendObjectQueue(){
        Map<String,Object> msg=new HashMap<>();
        msg.put("name","牛牛喵喵");
        msg.put("weight","160");
        rabbitTemplate.convertAndSend("object.queue",msg);
    }
}
