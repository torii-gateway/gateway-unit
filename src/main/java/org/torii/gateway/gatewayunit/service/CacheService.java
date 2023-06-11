package org.torii.gateway.gatewayunit.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.torii.gateway.gatewayunit.repository.CacheRepository;
import org.torii.gateway.gatewayunit.domain.ServiceResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheRepository cacheRepository;

    public Mono<ServiceResponse> save(String key, ServiceResponse value , int ttl) {
        try {
            return cacheRepository
                    .save(key, value, ttl)
                    .flatMap(saved -> {
                        if (saved) {
                            log.info("Cache saved for key {}", key);
                        } else {
                            log.info("Cache not saved for key {}", key);
                        }
                        return Mono.just(value);
                    });
        } catch (Exception ex) {
            log.error("Error while trying to save cache for key {}", key, ex);
        }
        return Mono.just(value);
    }

    public Mono<ServiceResponse> get(String key) {
        try {
            return cacheRepository
                    .get(key)
                    .doOnNext(response -> log.info("Cache retrieved for key {}", key));
        } catch (Exception ex) {
            log.error("Error while trying to retrieve cache for key {}", key, ex);
        }
        return Mono.empty();
    }

    public Mono<Boolean> existsForKey(String key) {
        try {
            return cacheRepository
                    .existsForKey(key)
                    .doOnNext(exists -> {
                        if (exists) {
                            log.info("Cache exists for the key {}", key);
                        } else {
                            log.info("Cache does not exist for the key {}", key);
                        }
                    });
        } catch (Exception ex) {
            log.error("Error while trying to check if cache exists for key {}", key, ex);
        }
        return Mono.just(false);
    }


}
