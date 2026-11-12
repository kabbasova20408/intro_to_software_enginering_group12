package cinematch;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;

/** Builds the JSON envelope used by every response. */
public final class Respond {

    private Respond() { }

    /** Success: { success, message, data } */
    public static void ok(Context ctx, int status, String message, Object data) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);
        ctx.status(status).json(body);
    }

    /** Success list: adds meta { total, page, perPage } */
    public static void list(Context ctx, String message, List<?> data, int total, int page, int perPage) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("total", total);
        meta.put("page", page);
        meta.put("perPage", perPage);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", true);
        body.put("message", message);
        body.put("data", data);
        body.put("meta", meta);
        ctx.status(200).json(body);
    }

    /** Error: { success: false, error: { code, message, details? } } */
    public static void error(Context ctx, int status, String code, String message,
                             List<Map<String, String>> details) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        if (details != null && !details.isEmpty()) {
            error.put("details", details);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("success", false);
        body.put("error", error);
        ctx.attribute("errorHandled", true);
        ctx.status(status).json(body);
    }
}
