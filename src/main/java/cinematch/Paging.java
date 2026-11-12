package cinematch;

import io.javalin.http.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Paging {

    private Paging() { }
    public static int[] read(Context ctx) {
        List<Map<String, String>> details = new ArrayList<>();
        int page = number(ctx.queryParam("page"), 1, "page", details);
        int perPage = number(ctx.queryParam("perPage"), 10, "perPage", details);
        if (details.isEmpty()) {
            if (page < 1) {
                details.add(ApiException.detail("page", "page must be 1 or greater."));
            }
            if (perPage < 1 || perPage > 100) {
                details.add(ApiException.detail("perPage", "perPage must be between 1 and 100."));
            }
        }
        if (!details.isEmpty()) {
            throw ApiException.validation(details);
        }
        return new int[] {page, perPage};
    }

    public static <T> List<T> slice(List<T> all, int page, int perPage) {
        long from = Math.min((long) (page - 1) * perPage, all.size());
        long to = Math.min(from + perPage, all.size());
        return all.subList((int) from, (int) to);
    }

    private static int number(String text, int defaultValue, String name, List<Map<String, String>> details) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            details.add(ApiException.detail(name, name + " must be an integer."));
            return defaultValue;
        }
    }
}
