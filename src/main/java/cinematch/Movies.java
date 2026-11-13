package cinematch;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Movies {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<Integer, Movie> byId = new LinkedHashMap<>();

    public Movies() {
        CSVFormat format = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build();
        try (Reader reader = new InputStreamReader(
                Movies.class.getClassLoader().getResourceAsStream("data/tmdb_5000_movies.csv"),
                StandardCharsets.UTF_8);
             CSVParser parser = format.parse(reader)) {
            for (CSVRecord r : parser) {
                Integer id = toInt(r.get("id"));
                if (id == null) {
                    continue;
                }
                String releaseDate = text(r.get("release_date"));
                Integer year = null;
                if (releaseDate != null && releaseDate.length() >= 4) {
                    year = toInt(releaseDate.substring(0, 4));
                }
                byId.put(id, new Movie(
                        id,
                        text(r.get("title")),
                        text(r.get("original_title")),
                        text(r.get("overview")),
                        text(r.get("tagline")),
                        names(r.get("genres")),
                        names(r.get("keywords")),
                        releaseDate,
                        year,
                        toInt(r.get("runtime")),
                        toDouble(r.get("vote_average")),
                        toInt(r.get("vote_count")),
                        toDouble(r.get("popularity")),
                        text(r.get("original_language")),
                        toLong(r.get("budget")),
                        toLong(r.get("revenue")),
                        text(r.get("homepage")),
                        text(r.get("status"))));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not load the movie CSV file.", e);
        }
    }

    public List<Movie> all() {
        return new ArrayList<>(byId.values());
    }

    public Movie find(int id) {
        return byId.get(id);
    }

    public boolean exists(int id) {
        return byId.containsKey(id);
    }

    private List<String> names(String json) {
        List<String> result = new ArrayList<>();
        if (json == null || json.isBlank()) {
            return result;
        }
        try {
            for (JsonNode node : mapper.readTree(json)) {
                JsonNode name = node.get("name");
                if (name != null) {
                    result.add(name.asText());
                }
            }
        } catch (IOException e) {
        }
        return result;
    }

    private static String text(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static Double toDouble(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer toInt(String s) {
        Double d = toDouble(s);
        return d == null ? null : d.intValue();
    }

    private static Long toLong(String s) {
        Double d = toDouble(s);
        return d == null ? null : d.longValue();
    }
}
