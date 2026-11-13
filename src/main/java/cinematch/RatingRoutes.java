package cinematch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class RatingRoutes {

    private record RatingInput(int movieId, int score, String comment) { }

    private static final Set<String> ALLOWED_FIELDS = Set.of("movieId", "score", "comment");

    private final Movies movies;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, Rating> ratings = new LinkedHashMap<>();
    private int counter = 104;

    public RatingRoutes(Movies movies) {
        this.movies = movies;
        add(new Rating("rating_101", 19995, 5, "Amazing visuals.", "user-a"));     
        add(new Rating("rating_102", 603, 4, "A classic sci-fi film.", "user-a")); 
        add(new Rating("rating_103", 27205, 5, "Mind-bending story.", "user-b"));  
        add(new Rating("rating_104", 597, 3, "Good, but too long.", "user-b"));  
    }

    private void add(Rating r) {
        ratings.put(r.id(), r);
    }

    public void register(Javalin app) {
        app.before("/api/v1/ratings", Auth::check);
        app.before("/api/v1/ratings/*", Auth::check);

        app.get("/api/v1/ratings", this::list);
        app.get("/api/v1/ratings/{id}", this::getOne);
        app.post("/api/v1/ratings", this::create);
        app.put("/api/v1/ratings/{id}", this::update);
        app.delete("/api/v1/ratings/{id}", this::delete);
    }

    private synchronized void list(Context ctx) {
        Auth.User user = ctx.attribute("user");
        int[] paging = Paging.read(ctx);

        List<Rating> visible = new ArrayList<>();
        for (Rating r : ratings.values()) {
            if (user.admin() || r.ownerId().equals(user.id())) {
                visible.add(r);
            }
        }
        Respond.list(ctx, "Ratings retrieved successfully.",
                Paging.slice(visible, paging[0], paging[1]), visible.size(), paging[0], paging[1]);
    }

    private synchronized void getOne(Context ctx) {
        Rating rating = findAllowed(ctx);
        Respond.ok(ctx, 200, "Rating retrieved successfully.", rating);
    }

    private synchronized void create(Context ctx) {
        Auth.User user = ctx.attribute("user");
        RatingInput input = validate(ctx.body());
        checkDuplicate(user.id(), input.movieId(), null);

        counter++;
        Rating rating = new Rating("rating_" + counter, input.movieId(), input.score(),
                input.comment(), user.id());
        add(rating);
        Respond.ok(ctx, 201, "Rating created successfully.", rating);
    }

    private synchronized void update(Context ctx) {
        Rating existing = findAllowed(ctx);
        RatingInput input = validate(ctx.body());
        checkDuplicate(existing.ownerId(), input.movieId(), existing.id());

        Rating updated = new Rating(existing.id(), input.movieId(), input.score(),
                input.comment(), existing.ownerId());
        add(updated);
        Respond.ok(ctx, 200, "Rating updated successfully.", updated);
    }

    private synchronized void delete(Context ctx) {
        Rating existing = findAllowed(ctx);
        ratings.remove(existing.id());
        Respond.ok(ctx, 200, "Rating deleted successfully.", null);
    }

    private Rating findAllowed(Context ctx) {
        Auth.User user = ctx.attribute("user");
        String id = ctx.pathParam("id");
        Rating rating = ratings.get(id);
        if (rating == null || (!user.admin() && !rating.ownerId().equals(user.id()))) {
            throw new ApiException(404, "NOT_FOUND", "Rating with id '" + id + "' was not found.");
        }
        return rating;
    }

    private void checkDuplicate(String ownerId, int movieId, String ignoreId) {
        for (Rating r : ratings.values()) {
            if (r.ownerId().equals(ownerId) && r.movieId() == movieId && !r.id().equals(ignoreId)) {
                throw new ApiException(409, "CONFLICT", "A rating for this movie already exists for this user.");
            }
        }
    }

    private RatingInput validate(String body) {
        if (body == null || body.isBlank()) {
            throw new ApiException(400, "BAD_REQUEST", "Request body is required and must be valid JSON.");
        }
        JsonNode json;
        try {
            json = mapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new ApiException(400, "BAD_REQUEST", "Request body is not valid JSON.");
        }
        if (json == null || !json.isObject()) {
            throw new ApiException(400, "BAD_REQUEST", "Request body must be a JSON object.");
        }

        List<Map<String, String>> details = new ArrayList<>();

        Iterator<String> names = json.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!ALLOWED_FIELDS.contains(name)) {
                details.add(ApiException.detail(name, "Unknown field. Allowed fields: movieId, score, comment."));
            }
        }

        Integer movieId = intField(json, "movieId", details);
        if (movieId != null) {
            if (movieId < 1) {
                details.add(ApiException.detail("movieId", "movieId must be a positive integer."));
            } else if (!movies.exists(movieId)) {
                details.add(ApiException.detail("movieId", "Movie with this id does not exist."));
            }
        }

        Integer score = intField(json, "score", details);
        if (score != null && (score < 1 || score > 5)) {
            details.add(ApiException.detail("score", "score must be between 1 and 5."));
        }

        String comment = null;
        JsonNode commentNode = json.get("comment");
        if (commentNode != null && !commentNode.isNull()) {
            if (!commentNode.isTextual()) {
                details.add(ApiException.detail("comment", "comment must be text."));
            } else {
                comment = commentNode.asText();
                if (comment.length() > 500) {
                    details.add(ApiException.detail("comment", "comment must not exceed 500 characters."));
                }
            }
        }

        if (!details.isEmpty()) {
            throw ApiException.validation(details);
        }
        return new RatingInput(movieId, score, comment);
    }

    private Integer intField(JsonNode json, String name, List<Map<String, String>> details) {
        JsonNode node = json.get(name);
        if (node == null || node.isNull()) {
            details.add(ApiException.detail(name, name + " is required."));
            return null;
        }
        if (!node.isIntegralNumber() || !node.canConvertToInt()) {
            details.add(ApiException.detail(name, name + " must be an integer."));
            return null;
        }
        return node.intValue();
    }
}
