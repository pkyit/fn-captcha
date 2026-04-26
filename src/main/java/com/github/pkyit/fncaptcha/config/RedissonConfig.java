package com.github.pkyit.fncaptcha.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 客户端配置
 * <p>从 Spring 配置文件读取 Redis 连接信息，构建 Redisson 单机模式客户端，
 * 支持密码认证和数据库索引配置。</p>
 */
@Configuration
public class RedissonConfig {

    private final String redisHost;
    private final int redisPort;
    private final String redisPassword;
    private final int redisDatabase;

    public RedissonConfig(
            @Value("${spring.data.redis.host:localhost}") String redisHost,
            @Value("${spring.data.redis.port:6379}") int redisPort,
            @Value("${spring.data.redis.password:}") String redisPassword,
            @Value("${spring.data.redis.database:0}") int redisDatabase) {
        this.redisHost = redisHost;
        this.redisPort = redisPort;
        this.redisPassword = redisPassword;
        this.redisDatabase = redisDatabase;
    }

    /**
     * 创建 Redisson 客户端 Bean
     * <p>使用单机模式连接 Redis，如果配置了密码则进行认证。</p>
     *
     * @return Redisson 客户端实例
     */
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        var serverConfig = config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setDatabase(redisDatabase);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            serverConfig.setPassword(redisPassword);
        }

        return Redisson.create(config);
    }
}
