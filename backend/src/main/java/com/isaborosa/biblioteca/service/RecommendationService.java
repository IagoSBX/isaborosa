package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.dto.RecommendationDto;
import com.isaborosa.biblioteca.service.recommendation.RecommendationStrategy;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Cacheia o resultado da estrategia de recomendacao por um periodo curto para
 * nao recalcular (e nao bater na Open Library) a cada acesso a pagina.
 */
@Service
public class RecommendationService {

    private static final Duration CACHE_TTL = Duration.ofHours(1);

    private final RecommendationStrategy strategy;

    private volatile List<RecommendationDto> cached = null;
    private volatile Instant cachedAt = Instant.MIN;

    public RecommendationService(RecommendationStrategy strategy) {
        this.strategy = strategy;
    }

    public synchronized List<RecommendationDto> getRecommendations() {
        if (cached != null && Duration.between(cachedAt, Instant.now()).compareTo(CACHE_TTL) < 0) {
            return cached;
        }
        cached = strategy.recommend();
        cachedAt = Instant.now();
        return cached;
    }
}
