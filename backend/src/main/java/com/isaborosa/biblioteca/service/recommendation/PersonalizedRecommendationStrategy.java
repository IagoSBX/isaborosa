package com.isaborosa.biblioteca.service.recommendation;

import com.isaborosa.biblioteca.dto.BookSearchResultDto;
import com.isaborosa.biblioteca.dto.RecommendationDto;
import com.isaborosa.biblioteca.integration.openlibrary.OpenLibraryClient;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Recomenda livros priorizando os generos mais frequentes entre os livros que
 * o usuario marcou como "ja li" (sinal principal - quanto mais ele leu de um
 * genero, mais isso pesa), complementado pelos autores dos livros bem
 * avaliados (nota >= 4, sinal secundario). Sem nenhum dos dois sinais, cai
 * para os autores do que ja foi lido (sinal mais fraco, mas melhor que nada).
 * Busca candidatos na Open Library e remove o que ja esta na biblioteca (em
 * qualquer status) - nunca recomenda um livro repetido. Regra simples, sem
 * IA; quanto mais o usuario avaliar/marcar livros como lidos, mais estes
 * sinais mudam e a recomendacao se adapta (nao ha modelo treinado, o "aprender"
 * vem de recalcular os sinais a cada chamada, com cache curto).
 */
@Component
public class PersonalizedRecommendationStrategy implements RecommendationStrategy {

    private static final int CANDIDATES_PER_SIGNAL = 12;
    private static final int MAX_RECOMMENDATIONS = 30;

    private final RecommendationSignalsLoader signalsLoader;
    private final OpenLibraryClient openLibraryClient;

    public PersonalizedRecommendationStrategy(
            RecommendationSignalsLoader signalsLoader, OpenLibraryClient openLibraryClient) {
        this.signalsLoader = signalsLoader;
        this.openLibraryClient = openLibraryClient;
    }

    @Override
    public List<RecommendationDto> recommend() {
        RecommendationSignals signals = signalsLoader.load();
        if (signals.topGenres().isEmpty() && signals.topAuthors().isEmpty()) {
            return List.of();
        }

        Set<String> ownedSignatures = signals.ownedSignatures();
        Map<String, RecommendationDto> recommendations = new LinkedHashMap<>();

        for (String genre : signals.topGenres()) {
            List<BookSearchResultDto> candidates = openLibraryClient.searchByGenre(genre, 0, CANDIDATES_PER_SIGNAL);
            String reason = "Porque você lê bastante " + genre;
            if (!addCandidates(recommendations, candidates, ownedSignatures, reason)) {
                return List.copyOf(recommendations.values());
            }
        }

        for (String author : signals.topAuthors()) {
            List<BookSearchResultDto> candidates = openLibraryClient.searchByAuthor(author, CANDIDATES_PER_SIGNAL);
            String reason = "Baseado nos livros de " + author + " na sua biblioteca";
            if (!addCandidates(recommendations, candidates, ownedSignatures, reason)) {
                return List.copyOf(recommendations.values());
            }
        }

        return List.copyOf(recommendations.values());
    }

    /** @return false quando o limite de recomendacoes foi atingido (para o chamador parar de buscar mais). */
    private boolean addCandidates(
            Map<String, RecommendationDto> recommendations,
            List<BookSearchResultDto> candidates,
            Set<String> ownedSignatures,
            String reason) {
        for (BookSearchResultDto candidate : candidates) {
            String signature = RecommendationSignatures.of(candidate.isbn(), candidate.openLibraryKey(), candidate.title());
            if (ownedSignatures.contains(signature) || recommendations.containsKey(signature)) {
                continue;
            }
            recommendations.put(signature, new RecommendationDto(candidate, reason));
            if (recommendations.size() >= MAX_RECOMMENDATIONS) {
                return false;
            }
        }
        return true;
    }
}
