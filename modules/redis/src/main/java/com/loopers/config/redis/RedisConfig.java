package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private static final String CONNECTION_MASTER = "redisConnectionMaster";
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Primary
    @Bean
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();
        return lettuceConnectionFactory(database, master, replicas, ReadFrom.REPLICA_PREFERRED);
    }

    @Qualifier(CONNECTION_MASTER)
    @Bean
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();
        return lettuceConnectionFactory(database, master, replicas, ReadFrom.MASTER);
    }

    @Primary
    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        return configureStringTemplate(template, lettuceConnectionFactory);
    }

    @Qualifier(REDIS_TEMPLATE_MASTER)
    @Bean
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        return configureStringTemplate(template, lettuceConnectionFactory);
    }

    private LettuceConnectionFactory lettuceConnectionFactory(
            int database,
            RedisNodeInfo master,
            List<RedisNodeInfo> replicas,
            ReadFrom readFrom
    ) {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(readFrom)
                .build();

        RedisStaticMasterReplicaConfiguration masterReplicaConfig =
                new RedisStaticMasterReplicaConfiguration(master.getHost(), master.getPort());
        masterReplicaConfig.setDatabase(database);

        if (replicas != null) {
            for (RedisNodeInfo replica : replicas) {
                masterReplicaConfig.addNode(replica.getHost(), replica.getPort());
            }
        }

        return new LettuceConnectionFactory(masterReplicaConfig, clientConfig);
    }

    private <K, V> RedisTemplate<K, V> configureStringTemplate(
            RedisTemplate<K, V> template,
            LettuceConnectionFactory connectionFactory
    ) {
        StringRedisSerializer s = new StringRedisSerializer();
        template.setKeySerializer(s);
        template.setValueSerializer(s);
        template.setHashKeySerializer(s);
        template.setHashValueSerializer(s);
        template.setConnectionFactory(connectionFactory);
        return template;
    }

}
