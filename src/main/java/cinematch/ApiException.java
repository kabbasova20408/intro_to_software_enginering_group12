package cinematch;

import java.util.List;
import java.util.Map;

public class ApiException extends RuntimeException {

    public final int status;
    public final String code;
    public final List<Map<String, String>> details; 

    public ApiException(int status, String code, String message) {
        this(status, code, message, null);
    }

    public ApiException(int status, String code, String message, List<Map<String, String>> details) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
    }

    public static Map<String, String> detail(String field, String message) {
        return Map.of("field", field, "message", message);
    }

    public static ApiException validation(List<Map<String, String>> details) {
        return new ApiException(400, "VALIDATION_ERROR",
                "The request contains invalid or missing fields.", details);
    }

    public static ApiException validation(String field, String message) {
        return validation(List.of(detail(field, message)));
    }
}

