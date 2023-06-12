package org.torii.gateway.gatewayunit.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import org.torii.gateway.gatewayunit.domain.ResponseData;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CacheRepository {

    private final ReactiveRedisTemplate<String, ResponseData> reactiveRedisTemplate;

    public Mono<Boolean> save(String key, ResponseData value , int ttl) {
        return reactiveRedisTemplate
                .opsForValue()
                .set(key, value)
                .then(reactiveRedisTemplate.expire(key, Duration.ofSeconds(ttl)))
                .onErrorResume(throwable -> {
                    log.error("failed to save data in Redis for key: {}", key, throwable);
                    return Mono.just(false);
                });
    }

    public Mono<ResponseData> get(String key) {
        return reactiveRedisTemplate
                .opsForValue()
                .get(key)
                .onErrorResume(throwable -> {
                    log.error("failed to get data from Redis for key: {}", key, throwable);
                    return Mono.empty();
                });
    }

    public Mono<Boolean> existsForKey(String key) {
        return reactiveRedisTemplate
                .hasKey(key)
                .onErrorResume(throwable -> {
                    log.error("failed to check if key exists in Redis for key: {}", key, throwable);
                    return Mono.just(false);
                });
    }

}