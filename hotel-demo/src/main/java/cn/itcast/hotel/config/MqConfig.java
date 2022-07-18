package cn.itcast.hotel.config;

import cn.itcast.hotel.constants.HotelMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(HotelMqConstants.EXCHANGE_NAME,true,false);
    }

    @Bean
    public Queue insertQueue(){
        return new Queue(HotelMqConstants.INSERT_QUEUE_NAME,true);
    }

    @Bean
    public Queue deleteQueue(){
        return new Queue(HotelMqConstants.DELETE_QUEUE_NAME,true);
    }

    @Bean
    public Binding insertQueueBinding(){
        return BindingBuilder.bind(insertQueue()).to(topicExchange()).with(HotelMqConstants.INSERT_QUEUE_NAME);
    }

    @Bean
    public Binding deleteQueueBinding(){
        return BindingBuilder.bind(deleteQueue()).to(topicExchange()).with(HotelMqConstants.DELETE_QUEUE_NAME);
    }

}
