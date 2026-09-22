package com.isaborosa.biblioteca.integration.openlibrary;

import java.util.HashMap;
import java.util.Map;

/**
 * Os "subjects" da Open Library sao tags livres em ingles (personagens,
 * lugares, temas...), nao generos prontos. Este mapa reconhece os subjects
 * que sao generos de verdade e os traduz para portugues (usado ao salvar um
 * livro); o mapa reverso permite voltar ao termo em ingles para pesquisar por
 * genero na API (usado pelo motor de recomendacao).
 */
public final class GenreTranslations {

    private static final Map<String, String> EN_TO_PT = Map.ofEntries(
            Map.entry("fiction", "Ficção"),
            Map.entry("fantasy", "Fantasia"),
            Map.entry("fantasy fiction", "Fantasia"),
            Map.entry("science fiction", "Ficção científica"),
            Map.entry("mystery", "Mistério"),
            Map.entry("mystery fiction", "Mistério"),
            Map.entry("detective and mystery stories", "Policial"),
            Map.entry("romance", "Romance"),
            Map.entry("love stories", "Romance"),
            Map.entry("horror", "Terror"),
            Map.entry("horror fiction", "Terror"),
            Map.entry("thriller", "Suspense"),
            Map.entry("adventure", "Aventura"),
            Map.entry("adventure fiction", "Aventura"),
            Map.entry("biography", "Biografia"),
            Map.entry("autobiography", "Autobiografia"),
            Map.entry("history", "História"),
            Map.entry("poetry", "Poesia"),
            Map.entry("drama", "Drama"),
            Map.entry("juvenile fiction", "Infantojuvenil"),
            Map.entry("juvenile literature", "Infantojuvenil"),
            Map.entry("young adult fiction", "Jovem adulto"),
            Map.entry("classic literature", "Literatura clássica"),
            Map.entry("historical fiction", "Ficção histórica"),
            Map.entry("crime", "Policial"),
            Map.entry("war stories", "Ficção de guerra"),
            Map.entry("humor", "Humor"),
            Map.entry("self-help", "Autoajuda"),
            Map.entry("philosophy", "Filosofia"),
            Map.entry("psychology", "Psicologia"),
            Map.entry("graphic novels", "Graphic novel"),
            Map.entry("comics", "Quadrinhos"),
            Map.entry("comics & graphic novels", "Quadrinhos"),
            Map.entry("short stories", "Contos"));

    private static final Map<String, String> PT_TO_EN = buildReverseMap();

    private GenreTranslations() {
    }

    private static Map<String, String> buildReverseMap() {
        Map<String, String> reverse = new HashMap<>();
        // Ordem de insercao do EN_TO_PT favorece o primeiro termo em ingles
        // encontrado para cada rotulo em portugues (ex.: "Fantasia" -> "fantasy",
        // nao "fantasy fiction"), pois HashMap.putIfAbsent mantem o primeiro.
        EN_TO_PT.forEach((english, portuguese) -> reverse.putIfAbsent(portuguese, english));
        return reverse;
    }

    public static String toPortuguese(String english) {
        return english == null ? null : EN_TO_PT.get(english.toLowerCase());
    }

    public static String toEnglish(String portuguese) {
        return portuguese == null ? null : PT_TO_EN.get(portuguese);
    }
}
