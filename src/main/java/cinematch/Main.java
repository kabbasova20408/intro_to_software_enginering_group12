package cinematch;

import io.javalin.Javalin;
import io.javalin.http.HttpResponseException;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        Movies movies = new Movies();

        Javalin app = Javalin.create(config -> {
            config.http.prefer405over404 = true;
        });

        Greeting.register(app);
        new MovieRoutes(movies).register(app);
        new RatingRoutes(movies).register(app);
        app.get("/health", ctx -> Respond.ok(ctx, 200, "Service is healthy.", Map.of("status", "UP")));


        app.exception(ApiException.class, (e, ctx) ->
                Respond.error(ctx, e.status, e.code, e.getMessage(), e.details));

        app.exception(HttpResponseException.class, (e, ctx) -> {
            int status = e.getStatus();
            if (status == 404) {
                Respond.error(ctx, 404, "NOT_FOUND", "Endpoint not found.", null);
            } else if (status == 405) {
                Respond.error(ctx, 405, "METHOD_NOT_ALLOWED",
                        "This HTTP method is not supported for this endpoint.", null);
            } else {
                Respond.error(ctx, status, "BAD_REQUEST", "The request could not be processed.", null);
            }
        });

        app.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace(); // goes to the server console only, never to the client
            Respond.error(ctx, 500, "INTERNAL_ERROR", "An unexpected error occurred.", null);
        });

        app.error(404, ctx -> {
            if (ctx.attribute("errorHandled") == null) {
                Respond.error(ctx, 404, "NOT_FOUND", "Endpoint not found.", null);
            }
        });
        app.error(405, ctx -> {
            if (ctx.attribute("errorHandled") == null) {
                Respond.error(ctx, 405, "METHOD_NOT_ALLOWED",
                        "This HTTP method is not supported for this endpoint.", null);
            }
        });

        app.start(8080);
    }
}
