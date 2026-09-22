package com.isaborosa.biblioteca.service.recommendation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import com.isaborosa.biblioteca.domain.userbook.UserBook;
import com.isaborosa.biblioteca.domain.userbook.UserBookRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecommendationSignalsLoaderTest {

    @Mock
    private UserBookRepository userBookRepository;
    @Mock
    private UserRepository userRepository;

    private RecommendationSignalsLoader loader;
    private User user;

    @BeforeEach
    void setUp() {
        loader = new RecommendationSignalsLoader(userBookRepository, userRepository);
        user = new User("Isaborosa");
        setId(user, 1L);
        when(userRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(user));
    }

    private Book book(String title, String author, String genre) {
        return new Book(title, author, null, null, 2000, null, null, genre, null, null);
    }

    @Test
    void semLivroLidoENotaAltaNaoGeraNenhumSinal() {
        Book livro = book("Livro qualquer", "Autor Z", "Fantasia");
        UserBook entry = new UserBook(user, livro, ReadingStatus.LENDO, 3);
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of(entry));

        RecommendationSignals signals = loader.load();

        assertThat(signals.topGenres()).isEmpty();
        assertThat(signals.topAuthors()).isEmpty();
    }

    @Test
    void priorizaGeneroDosLivrosJaLidosPorFrequencia() {
        List<UserBook> entries = List.of(
                new UserBook(user, book("F1", "Autor A", "Fantasia"), ReadingStatus.LIDO, null),
                new UserBook(user, book("F2", "Autor B", "Fantasia"), ReadingStatus.LIDO, null),
                new UserBook(user, book("R1", "Autor C", "Romance"), ReadingStatus.LIDO, null));
        when(userBookRepository.findAllByUserId(1L)).thenReturn(entries);

        RecommendationSignals signals = loader.load();

        assertThat(signals.topGenres()).containsExactly("Fantasia", "Romance");
    }

    @Test
    void ignoraGeneroDeLivroAindaNaoLido() {
        Book livro = book("Livro qualquer", "Autor Z", "Fantasia");
        UserBook entry = new UserBook(user, livro, ReadingStatus.QUERO_LER, null);
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of(entry));

        RecommendationSignals signals = loader.load();

        assertThat(signals.topGenres()).isEmpty();
    }

    @Test
    void usaAutorDeAvaliacaoAltaComoSinalSecundarioIndependenteDoStatus() {
        Book livro = book("Livro qualquer", "Autor Z", null);
        UserBook entry = new UserBook(user, livro, ReadingStatus.QUERO_LER, 5);
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of(entry));

        RecommendationSignals signals = loader.load();

        assertThat(signals.topAuthors()).containsExactly("Autor Z");
    }

    @Test
    void caiParaAutorDeLivroLidoQuandoNaoHaGeneroNemNotaAlta() {
        Book livro = book("Livro qualquer", "Autor Z", null);
        UserBook entry = new UserBook(user, livro, ReadingStatus.LIDO, null);
        when(userBookRepository.findAllByUserId(1L)).thenReturn(List.of(entry));

        RecommendationSignals signals = loader.load();

        assertThat(signals.topAuthors()).containsExactly("Autor Z");
    }

    @Test
    void constroiAssinaturasDosLivrosJaPossuidos() {
        Book withIsbn = new Book("Com ISBN", "Autor X", "9788500000000", null, null, null, null, null, null, null);
        Book withKey = new Book("Com chave", "Autor Y", null, null, null, null, null, null, null, "/works/OLabc");

        List<UserBook> entries = List.of(
                new UserBook(user, withIsbn, ReadingStatus.QUERO_LER, null),
                new UserBook(user, withKey, ReadingStatus.QUERO_LER, null));
        when(userBookRepository.findAllByUserId(1L)).thenReturn(entries);

        RecommendationSignals signals = loader.load();

        assertThat(signals.ownedSignatures()).containsExactlyInAnyOrder("isbn:9788500000000", "key:/works/OLabc");
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
