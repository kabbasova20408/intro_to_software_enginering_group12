package cinematch;

import io.javalin.http.Context;
import java.util.Map;

public final class Auth {

    public record User(String id, boolean admin) { }

    private static final Map<String, User> TOKENS = Map.of(
            "user-a-token", new User("user-a", false),
            "user-b-token", new User("user-b", false),
            "admin-token", new User("admin", true));

    private Auth() { }

   
    public static void check(Context ctx) {
        String header = ctx.header("Authorization");
        if (header == null || header.isBlank()) {
            throw new ApiException(401, "UNAUTHORIZED",
                    "Token is missing. Send the header 'Authorization: Bearer <token>'.");
        }
        if (!header.startsWith("Bearer ")) {
            throw new ApiException(401, "UNAUTHORIZED",
                    "Authorization header must look like 'Bearer <token>'.");
        }
        User user = TOKENS.get(header.substring(7).trim());
        if (user == null) {
            throw new ApiException(401, "UNAUTHORIZED", "The token is invalid.");
        }
        ctx.attribute("user", user);
    }
}

