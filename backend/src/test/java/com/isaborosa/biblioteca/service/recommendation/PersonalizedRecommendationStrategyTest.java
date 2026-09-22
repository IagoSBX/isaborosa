package com.isaborosa.biblioteca.service.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.isaborosa.biblioteca.dto.BookSearchResultDto;
import com.isaborosa.biblioteca.dto.RecommendationDto;
import com.isaborosa.biblioteca.integration.openlibrary.OpenLibraryClient;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PersonalizedRecommendationStrategyTest {

    @Mock
    private RecommendationSignalsLoader signalsLoader;
    @Mock
    private OpenLibraryClient openLibraryClient;

    @Test
    void naoRecomendaNadaQuandoNaoHaSinalNenhum() {
        when(signalsLoader.load()).thenReturn(new RecommendationSignals(List.of(), List.of(), Set.of()));

        PersonalizedRecommendationStrategy strategy =
                new PersonalizedRecommendationStrategy(signalsLoader, openLibraryClient);

        List<RecommendationDto> result = strategy.recommend();

        assertThat(result).isEmpty();
        verifyNoInteractions(openLibraryClient);
    }

    @Test
    void priorizaGeneroMaisFrequenteEExcluiOsJaPossuidos() {
        String ownedSignature = "key:/works/OL27482W";
        when(signalsLoader.load()).thenReturn(
                new RecommendationSignals(List.of("Fantasia"), List.of(), Set.of(ownedSignature)));

        BookSearchResultDto alreadyOwned = new BookSearchResultDto(
                "O Hobbit", "J.R.R. Tolkien", null, null, 1937, null, null, "Fantasia", "/works/OL27482W");
        BookSearchResultDto newSuggestion = new BookSearchResultDto(
                "A Guerra dos Tronos", "George R. R. Martin", null, null, 1996, null, null, "Fantasia", "/works/OLgot");

        when(openLibraryClient.searchByGenre(eq("Fantasia"), anyInt(), anyInt()))
                .thenReturn(List.of(alreadyOwned, newSuggestion));

        PersonalizedRecommendationStrategy strategy =
                new PersonalizedRecommendationStrategy(signalsLoader, openLibraryClient);

        List<RecommendationDto> result = strategy.recommend();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).book().title()).isEqualTo("A Guerra dos Tronos");
        assertThat(result.get(0).reason()).contains("Fantasia");
        verify(openLibraryClient, never()).searchByAuthor(any(), anyInt());
    }

    @Test
    void combinaGeneroEAutorSemDuplicar() {
        when(signalsLoader.load()).thenReturn(
                new RecommendationSignals(List.of("Fantasia"), List.of("J.R.R. Tolkien"), Set.of()));

        BookSearchResultDto fromGenre = new BookSearchResultDto(
                "Livro A", "Autor A", null, null, 2000, null, null, "Fantasia", "/works/OLa");
        BookSearchResultDto fromAuthor = new BookSearchResultDto(
                "Livro B", "J.R.R. Tolkien", null, null, 2001, null, null, "Fantasia", "/works/OLb");

        when(openLibraryClient.searchByGenre(eq("Fantasia"), anyInt(), anyInt())).thenReturn(List.of(fromGenre));
        when(openLibraryClient.searchByAuthor(eq("J.R.R. Tolkien"), anyInt())).thenReturn(List.of(fromAuthor));

        PersonalizedRecommendationStrategy strategy =
                new PersonalizedRecommendationStrategy(signalsLoader, openLibraryClient);

        List<RecommendationDto> result = strategy.recommend();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(r -> r.book().title()).containsExactlyInAnyOrder("Livro A", "Livro B");
    }

    @Test
    void deduplicaCandidatosRepetidosEntreGeneros() {
        when(signalsLoader.load()).thenReturn(
                new RecommendationSignals(List.of("Fantasia", "Aventura"), List.of(), Set.of()));

        BookSearchResultDto shared = new BookSearchResultDto(
                "Livro Compartilhado", "Autor X", null, null, 2000, null, null, "Fantasia", "/works/OLshared");

        when(openLibraryClient.searchByGenre(eq("Fantasia"), anyInt(), anyInt())).thenReturn(List.of(shared));
        when(openLibraryClient.searchByGenre(eq("Aventura"), anyInt(), anyInt())).thenReturn(List.of(shared));

        PersonalizedRecommendationStrategy strategy =
                new PersonalizedRecommendationStrategy(signalsLoader, openLibraryClient);

        List<RecommendationDto> result = strategy.recommend();

        assertThat(result).hasSize(1);
    }
}
