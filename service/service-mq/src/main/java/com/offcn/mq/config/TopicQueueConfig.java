package com.offcn.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TopicQueueConfig {

    //声明2个队列名称
    public static final String topic1 = "topic_queue_1";
    public static final String topic2 = "topic_queue_2";

    //声明一个交换机名称
    public static final String topicExchange = "topicExchange";

    //声明队列1
    @Bean
    public Queue topic1Queue(){
        return new Queue(topic1,true);
    }
    //声明队列2
    @Bean
    public Queue topic2Queue(){
        return new Queue(topic2,true);
    }

    //声明交换机 主题模式
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(topicExchange,true,true);
    }

    //绑定队列1到交换机 指定路由key    topic.* 不多不少正好一个词
    @Bean
    public Binding bindingTopic1QueueToExchange(Queue topic1Queue,TopicExchange topicExchange){
        return BindingBuilder.bind(topic1Queue).to(topicExchange).with("topic.*");
    }

    //绑定队列2到交换机 指定路由key  topic.#  一个或者多个词
    @Bean
    public Binding bindingTopic2QueueToExchange(Queue topic2Queue,TopicExchange topicExchange){
        return BindingBuilder.bind(topic2Queue).to(topicExchange).with("topic.#");
    }
}
