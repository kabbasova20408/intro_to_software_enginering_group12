package cinematch;

import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.List;

public class MovieRoutes {

    private final Movies movies;

    public MovieRoutes(Movies movies) {
        this.movies = movies;
    }

    public void register(Javalin app) {
        app.get("/api/v1/movies", this::list);
        app.get("/api/v1/movies/{id}", this::getOne);
    }

    private void list(Context ctx) {
        int[] paging = Paging.read(ctx);
        String search = ctx.queryParam("search");
        String genre = ctx.queryParam("genre");
        String yearText = ctx.queryParam("year");

        Integer year = null;
        if (yearText != null && !yearText.isBlank()) {
            try {
                year = Integer.valueOf(yearText.trim());
            } catch (NumberFormatException e) {
                throw ApiException.validation("year", "year must be an integer, for example 2009.");
            }
        }

        List<Movie> result = new ArrayList<>();
        for (Movie m : movies.all()) {
            if (search != null && !search.isBlank()
                    && (m.title() == null || !m.title().toLowerCase().contains(search.trim().toLowerCase()))) {
                continue;
            }
            if (genre != null && !genre.isBlank()
                    && m.genres().stream().noneMatch(g -> g.equalsIgnoreCase(genre.trim()))) {
                continue;
            }
            if (year != null && !year.equals(m.year())) {
                continue;
            }
            result.add(m);
        }
        Respond.list(ctx, "Movies retrieved successfully.",
                Paging.slice(result, paging[0], paging[1]), result.size(), paging[0], paging[1]);
    }

    private void getOne(Context ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw ApiException.validation("id", "id must be an integer.");
        }
        Movie movie = movies.find(id);
        if (movie == null) {
            throw new ApiException(404, "NOT_FOUND", "Movie with id '" + id + "' was not found.");
        }
        Respond.ok(ctx, 200, "Movie retrieved successfully.", movie);
    }
}
