package main.java.cinematch;

import java.util.List;

/** One movie from the TMDB CSV. */
public record Movie(
        int id,
        String title,
        String originalTitle,
        String overview,
        String tagline,
        List<String> genres,
        List<String> keywords,
        String releaseDate,
        Integer year,
        Integer runtime,
        Double voteAverage,
        Integer voteCount,
        Double popularity,
        String originalLanguage,
        Long budget,
        Long revenue,
        String homepage,
        String status) {
}
