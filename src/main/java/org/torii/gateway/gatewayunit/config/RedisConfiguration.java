package org.torii.gateway.gatewayunit.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.torii.gateway.gatewayunit.domain.ServiceResponse;

@Configuration
public class RedisConfiguration {

    @Bean
    public ReactiveRedisTemplate<String, ServiceResponse> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ServiceResponse> valueSerializer =
                new Jackson2JsonRedisSerializer<>(ServiceResponse.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, ServiceResponse> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, ServiceResponse> context =
                builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }


}
