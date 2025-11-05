package cn.sym.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class RocketMQConfig {
    
    @Bean
    public RocketMQTemplate rocketMQTemplate() {
        RocketMQTemplate rocketMQTemplate = new RocketMQTemplate();
        
        // 设置自定义消息转换器
        rocketMQTemplate.setMessageConverter(jackson2MessageConverter());
        
        // 设置异步发送的回调执行器
        rocketMQTemplate.setAsyncSenderExecutor(new ThreadPoolExecutor(
                5,          // 核心线程数
                10,         // 最大线程数
                60L,        // 空闲线程存活时间
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),  // 任务队列
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        ));
        
        // 设置事务监听器执行器（如果使用事务消息）
        // 使用反射方式设置，因为RocketMQTemplate没有提供直接的setter方法
        try {
            java.lang.reflect.Field executorServiceField = RocketMQTemplate.class.getDeclaredField("executorService");
            executorServiceField.setAccessible(true);
            executorServiceField.set(rocketMQTemplate, Executors.newFixedThreadPool(5));
        } catch (Exception e) {
            // 如果无法设置，则忽略
            e.printStackTrace();
        }

        // 设置检查线程池
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        // 使用反射设置checkExecutor，因为RocketMQTemplate没有直接的setter方法
        try {
            java.lang.reflect.Field field = RocketMQTemplate.class.getDeclaredField("checkExecutor");
            field.setAccessible(true);
            field.set(rocketMQTemplate, scheduledExecutorService);
        } catch (Exception e) {
            // 如果无法设置，则忽略
            e.printStackTrace();
        }

        return rocketMQTemplate;
    }
    
    /**
     * 自定义Jackson消息转换器
     * 支持Java 8时间类型等特性
     * @return converter
     */
    @Bean
    public MessageConverter jackson2MessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        
        // 自定义ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册JavaTimeModule以支持Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 设置ObjectMapper
        converter.setObjectMapper(objectMapper);
        
        // 设置编码方式
        converter.setSerializedPayloadClass(String.class);
        
        return converter;
    }
}
