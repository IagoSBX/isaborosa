package com.isaborosa.biblioteca.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import com.isaborosa.biblioteca.domain.userbook.UserBook;
import com.isaborosa.biblioteca.domain.userbook.UserBookRepository;
import com.isaborosa.biblioteca.dto.StatsDto;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private UserBookRepository userBookRepository;
    @Mock
    private UserRepository userRepository;

    private StatsService statsService;
    private User user;

    @BeforeEach
    void setUp() {
        statsService = new StatsService(userBookRepository, userRepository);
        user = new User("Isaborosa");
        setId(user, 1L);
        when(userRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(user));
    }

    @Test
    void semLivrosRetornaTotaisZeradosENulos() {
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of());

        StatsDto stats = statsService.getStats();

        assertThat(stats.totalBooks()).isZero();
        assertThat(stats.totalPagesRead()).isNull();
        assertThat(stats.averageRating()).isNull();
        assertThat(stats.genreDistribution()).isEmpty();
        assertThat(stats.booksByStatus().get(ReadingStatus.LIDO)).isZero();
    }

    @Test
    void somaPaginasApenasDosLivrosMarcadosComoLido() {
        Book lido = new Book("Lido", "Autor", null, null, null, null, 300, "Fantasy", null, null);
        Book lendo = new Book("Lendo", "Autor", null, null, null, null, 500, "Fantasy", null, null);
        Book lidoSemPaginas = new Book("Lido sem paginas", "Autor", null, null, null, null, null, null, null, null);

        List<UserBook> entries = List.of(
                new UserBook(user, lido, ReadingStatus.LIDO, 5),
                new UserBook(user, lendo, ReadingStatus.LENDO, null),
                new UserBook(user, lidoSemPaginas, ReadingStatus.LIDO, 4));
        when(userBookRepository.findAllByUserId(1L)).thenReturn(entries);

        StatsDto stats = statsService.getStats();

        assertThat(stats.totalBooks()).isEqualTo(3);
        assertThat(stats.totalPagesRead()).isEqualTo(300L);
        assertThat(stats.booksByStatus().get(ReadingStatus.LIDO)).isEqualTo(2);
        assertThat(stats.booksByStatus().get(ReadingStatus.LENDO)).isEqualTo(1);
        assertThat(stats.averageRating()).isEqualTo(4.5);
    }

    @Test
    void agregaDistribuicaoDeGenerosOrdenadaPorFrequencia() {
        Book fantasyA = new Book("A", "Autor", null, null, null, null, null, "Fantasy", null, null);
        Book fantasyB = new Book("B", "Autor", null, null, null, null, null, "Fantasy", null, null);
        Book scifi = new Book("C", "Autor", null, null, null, null, null, "Ficcao cientifica", null, null);
        Book semGenero = new Book("D", "Autor", null, null, null, null, null, null, null, null);

        List<UserBook> entries = List.of(
                new UserBook(user, fantasyA, ReadingStatus.LIDO, null),
                new UserBook(user, fantasyB, ReadingStatus.QUERO_LER, null),
                new UserBook(user, scifi, ReadingStatus.LIDO, null),
                new UserBook(user, semGenero, ReadingStatus.LIDO, null));
        when(userBookRepository.findAllByUserId(1L)).thenReturn(entries);

        StatsDto stats = statsService.getStats();

        assertThat(stats.genreDistribution()).hasSize(2);
        assertThat(stats.genreDistribution().get(0).genre()).isEqualTo("Fantasy");
        assertThat(stats.genreDistribution().get(0).count()).isEqualTo(2);
        assertThat(stats.genreDistribution().get(1).genre()).isEqualTo("Ficcao cientifica");
        assertThat(stats.genreDistribution().get(1).count()).isEqualTo(1);
    }

    @Test
    void distribuicaoDeAvaliacoesSempreTemAsCincoNotasMesmoZeradas() {
        Book livro = new Book("A", "Autor", null, null, null, null, null, null, null, null);
        List<UserBook> entries = List.of(
                new UserBook(user, livro, ReadingStatus.LIDO, 5),
                new UserBook(user, livro, ReadingStatus.LIDO, 5),
                new UserBook(user, livro, ReadingStatus.LIDO, 3));
        when(userBookRepository.findAllByUserId(1L)).thenReturn(entries);

        StatsDto stats = statsService.getStats();

        assertThat(stats.ratingDistribution()).hasSize(5);
        assertThat(stats.ratingDistribution().get(4).rating()).isEqualTo(5);
        assertThat(stats.ratingDistribution().get(4).count()).isEqualTo(2);
        assertThat(stats.ratingDistribution().get(2).rating()).isEqualTo(3);
        assertThat(stats.ratingDistribution().get(2).count()).isEqualTo(1);
        assertThat(stats.ratingDistribution().get(0).count()).isZero();
    }

    @Test
    void evolucaoDeLeituraAgrupaPorMesDaUltimaAtualizacao() {
        Book livro = new Book("A", "Autor", null, null, null, null, null, null, null, null);
        UserBook jan = new UserBook(user, livro, ReadingStatus.LIDO, 5);
        UserBook fev = new UserBook(user, livro, ReadingStatus.LIDO, 4);
        setUpdatedAt(jan, ZonedDateTime.of(2026, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant());
        setUpdatedAt(fev, ZonedDateTime.of(2026, 2, 10, 12, 0, 0, 0, ZoneOffset.UTC).toInstant());
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of(jan, fev));

        StatsDto stats = statsService.getStats();

        assertThat(stats.readingEvolution()).hasSize(2);
        assertThat(stats.readingEvolution().get(0).month()).isEqualTo("2026-01");
        assertThat(stats.readingEvolution().get(0).count()).isEqualTo(1);
        assertThat(stats.readingEvolution().get(1).month()).isEqualTo("2026-02");
        assertThat(stats.readingEvolution().get(1).count()).isEqualTo(1);
    }

    private void setUpdatedAt(UserBook userBook, Instant instant) {
        try {
            var field = UserBook.class.getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(userBook, instant);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private void setId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
