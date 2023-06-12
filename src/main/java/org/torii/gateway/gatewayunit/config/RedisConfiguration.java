package org.torii.gateway.gatewayunit.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.torii.gateway.gatewayunit.domain.ResponseData;

@Configuration
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, ResponseData> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ResponseData> valueSerializer =
                new Jackson2JsonRedisSerializer<>(ResponseData.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, ResponseData> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, ResponseData> context =
                builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }


}
