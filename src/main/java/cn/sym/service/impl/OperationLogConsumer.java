package cn.sym.service.impl;

import cn.sym.dto.OperationLogDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RocketMQMessageListener(topic = "operation-log-topic", 
                        consumerGroup = "operation-log-consumer-group",
        consumeThreadNumber = 10)
public class OperationLogConsumer implements RocketMQListener<OperationLogDTO> {
    
    private final StringRedisTemplate redisTemplate;
    
    // 统计各类操作的数量
    private static final String OPERATION_COUNT_PREFIX = "operation_count_";
    // 用户活跃度统计
    private static final String USER_ACTIVITY_PREFIX = "user_activity_";
    // 操作类型排行榜
    private static final String OPERATION_RANKING_KEY = "operation_ranking";

    public OperationLogConsumer(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(OperationLogDTO message) {
        log.info("接收到操作日志消息: 用户ID={}, 操作类型={}, 操作内容={}", 
                message.getUserId(), message.getOperationType(), message.getContent());
        
        // 在这里处理操作日志的业务逻辑
        // 例如：保存到数据库、分析用户行为等
        saveOperationLog(message);
        
        // 增加额外的日志处理逻辑
        processOperationAnalysis(message);
        updateUserActivity(message);
        updateOperationRanking(message);
    }
    
    private void saveOperationLog(OperationLogDTO message) {
        // 模拟保存操作日志到数据库
        log.info("保存操作日志到数据库: 用户ID={}, 操作类型={}, 操作内容={}", 
                message.getUserId(), message.getOperationType(), message.getContent());
        
        // 实际项目中，这里会调用数据库操作保存日志
        // operationLogRepository.save(operationLog);
        
        try {
            // 同时将日志保存到Redis中，便于快速查询
            String logKey = "operation_log_" + System.currentTimeMillis() + "_" + message.getUserId();
            Map<String, String> logData = new HashMap<>();
            logData.put("userId", String.valueOf(message.getUserId()));
            logData.put("operationType", message.getOperationType());
            logData.put("content", message.getContent());
            logData.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            redisTemplate.opsForHash().putAll(logKey, logData);
            // 设置过期时间，避免占用过多内存
            redisTemplate.expire(logKey, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error("保存操作日志到Redis失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 处理操作分析统计
     * @param message 操作日志消息
     */
    private void processOperationAnalysis(OperationLogDTO message) {
        try {
            String countKey = OPERATION_COUNT_PREFIX + message.getOperationType();
            // 增加该操作类型的计数
            redisTemplate.opsForValue().increment(countKey, 1);
            // 设置过期时间，按天统计
            redisTemplate.expire(countKey, 1, TimeUnit.DAYS);
            
            log.debug("操作类型统计已更新: {} (+1)", message.getOperationType());
        } catch (Exception e) {
            log.error("处理操作分析统计失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 更新用户活跃度
     * @param message 操作日志消息
     */
    private void updateUserActivity(OperationLogDTO message) {
        try {
            String activityKey = USER_ACTIVITY_PREFIX + message.getUserId();
            // 增加用户活跃度计数
            redisTemplate.opsForValue().increment(activityKey, 1);
            // 设置过期时间，按月统计
            redisTemplate.expire(activityKey, 30, TimeUnit.DAYS);
            
            log.debug("用户活跃度已更新: 用户ID {} (+1)", message.getUserId());
        } catch (Exception e) {
            log.error("更新用户活跃度失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 更新操作类型排行榜
     * @param message 操作日志消息
     */
    private void updateOperationRanking(OperationLogDTO message) {
        try {
            // 将操作类型加入排行榜，分数为操作次数
            redisTemplate.opsForZSet().incrementScore(OPERATION_RANKING_KEY, message.getOperationType(), 1);
            log.debug("操作类型排行榜已更新: {} (+1)", message.getOperationType());
        } catch (Exception e) {
            log.error("更新操作类型排行榜失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 获取操作类型统计
     * @return 操作类型统计Map
     */
    public Map<String, String> getOperationStatistics() {
        try {
            // 获取所有匹配的操作统计键
            Set<String> keys = redisTemplate.keys(OPERATION_COUNT_PREFIX + "*");
            Map<String, String> result = new HashMap<>();
            
            if (keys != null) {
                for (String key : keys) {
                    String value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        // 提取操作类型名称（去掉前缀）
                        String operationType = key.substring(OPERATION_COUNT_PREFIX.length());
                        result.put(operationType, value);
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取操作统计失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 获取操作类型排行
     * @param topN 前N名
     * @return 操作类型排行
     */
    public Map<String, Double> getUserActivityRanking(int topN) {
        try {
            Set<ZSetOperations.TypedTuple<String>> ranking = redisTemplate.opsForZSet().reverseRangeWithScores(OPERATION_RANKING_KEY, 0, topN - 1);
            Map<String, Double> result = new LinkedHashMap<>();
            
            if (ranking != null) {
                for (ZSetOperations.TypedTuple<String> tuple : ranking) {
                    if (tuple.getValue() != null && tuple.getScore() != null) {
                        result.put(tuple.getValue(), tuple.getScore());
                    }
                }
            }
            
            return result;
        } catch (Exception e) {
            log.error("获取操作类型排行失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
}

