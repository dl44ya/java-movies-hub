package ru.practicum.moviehub.http;

import com.google.gson.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final int PORT = 8080;
    private static final String BASE = "http://localhost:";
    private static MoviesServer server;
    private static HttpClient client;
    private static final Gson gson = new Gson();
    private static MoviesStore movieStore = new MoviesStore();

    @BeforeEach
    void beforeEach() {
        movieStore.clearMoviesStore();
    }

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(movieStore, PORT);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    // GET
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        assertTrue(jsonElement.isJsonArray(), "Ожидается JSON-массив");
        assertTrue(jsonArray.isEmpty(), "Ожидается пустой JSON-массив");
    }

    @Test
    void getMovies_wentNotEmpty_returnsMoviesArray() throws Exception {
        movieStore.addMovie(new Movie("Title1", 2000));
        movieStore.addMovie(new Movie("Title2", 2015));
        movieStore.addMovie(new Movie("Title3", 2020));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Movie> movies = gson.fromJson(jsonArray, new ListOfMoviesTypeToken().getType());

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
        assertTrue(jsonElement.isJsonArray(), "Ожидается JSON-массив");
        assertEquals(movies.get(0), movieStore.getAllMoviesList().get(0));
        assertEquals(movies.get(1), movieStore.getAllMoviesList().get(1));
        assertEquals(movies.get(2), movieStore.getAllMoviesList().get(2));
    }

    // GET by ID
    @Test
    void getMoviesById_whenNotEmpty_returnsMovie() throws Exception {
        movieStore.addMovie(new Movie("Title1", 2000));
        movieStore.addMovie(new Movie("Title2", 2015));
        movieStore.addMovie(new Movie("Title3", 2020));

        int id = movieStore.getAllMoviesList().getFirst().getId();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies/" + id))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        Movie movie = gson.fromJson(jsonElement, Movie.class);
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals(movie, movieStore.getAllMoviesList().getFirst());
    }

    @Test
    void getMoviesById_whenEmpty_returnsErrorMessage() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies/" + 100))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();

        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Фильм не найден", errorMessage);
    }

    @Test
    void getMoviesById_whenInvalidId_returnsErrorMessage() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies/10f"))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Некорректный ID", errorMessage);
    }

    // GET by YEAR
    @Test
    void getMoviesByYear_whenNotEmpty_returnsMovie() throws Exception {
        movieStore.addMovie(new Movie("Title1", 2000));
        movieStore.addMovie(new Movie("Title2", 2015));
        movieStore.addMovie(new Movie("Title3", 2020));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies?year=2000"))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Movie> movies = gson.fromJson(jsonArray, new ListOfMoviesTypeToken().getType());
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
        assertTrue(jsonElement.isJsonArray(), "Ожидается JSON-массив");
        assertEquals(movies.getFirst(), movieStore.getAllMoviesList().getFirst());
    }

    @Test
    void getMoviesByYear_whenEmpty_returnsEmptyArray() throws Exception {
        movieStore.addMovie(new Movie("Title1", 2000));
        movieStore.addMovie(new Movie("Title2", 2015));
        movieStore.addMovie(new Movie("Title3", 2020));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies?year=2018"))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonArray jsonArray = jsonElement.getAsJsonArray();
        List<Movie> movies = gson.fromJson(jsonArray, new ListOfMoviesTypeToken().getType());
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");
        assertTrue(jsonElement.isJsonArray(), "Ожидается JSON-массив");
        assertTrue(movies.isEmpty(), "Ожидается пустой JSON-массив");
    }

    @Test
    void getMoviesByYear_whenInvalidYear_returnsErrorMessage() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies?year=20mn"))
                .GET()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Некорректный параметр запроса — year", errorMessage);
    }

    // POST
    @Test
    void postMovie_whenCorrectData_returnsMovie() throws Exception {
        String requestBody = "{\"title\":\"Title\",\"year\":1999}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        Movie movie = gson.fromJson(jsonElement, Movie.class);
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(201, resp.statusCode(), "GET /movies должен вернуть 201");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals(movie, movieStore.getAllMoviesList().getFirst());
    }

    @Test
    void postMovie_whenEmptyTitle_returnsMovie() throws Exception {
        String requestBody = "{\"title\":\"\",\"year\":1999}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();
        String errorDetails = jsonObject.get("details").getAsString();

        assertEquals(422, resp.statusCode(), "GET /movies должен вернуть 422");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Название не должно быть пустым или длинее 100 символов", errorDetails);
        assertEquals("Ошибка валидации", errorMessage);
    }

    @Test
    void postMovie_whenLongTitle_returnsErrorMessage() throws Exception {
        String longTitle = "g".repeat(101);
        String requestBody = "{\"title\":\""
                + longTitle
                + "\",\"year\":1999}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();
        String errorDetails = jsonObject.get("details").getAsString();

        assertEquals(422, resp.statusCode(), "GET /movies должен вернуть 422");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Название не должно быть пустым или длинее 100 символов", errorDetails);
        assertEquals("Ошибка валидации", errorMessage);
    }

    @Test
    void postMovie_whenInvalidYear_returnsErrorMessage() throws Exception {
        String requestBody = "{\"title\":\"Title\",\"year\":2300}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();
        String errorDetails = jsonObject.get("details").getAsString();

        assertEquals(422, resp.statusCode(), "GET /movies должен вернуть 422");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Год должен быть между 1888 и 2026", errorDetails);
        assertEquals("Ошибка валидации", errorMessage);
    }

    @Test
    void postMovie_whenInvalidRequestHeader_returnsErrorMessage() throws Exception {
        String requestBody = "{\"title\":\"Title\",\"year\":1999}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "text/plain")
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(415, resp.statusCode(), "GET /movies должен вернуть 415");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Неподдерживаемый тип данных", errorMessage);
    }

    @Test
    void postMovie_whenInvalidJson_returnsErrorMessage() throws Exception {
        String requestBody = "{\"title\":\"Title\"}";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(415, resp.statusCode(), "GET /movies должен вернуть 415");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Неподдерживаемый тип данных", errorMessage);
    }

    // DELETE
    @Test
    void deleteMovie_whenMovieExist() throws Exception {
        movieStore.addMovie(new Movie("Title1", 2000));
        movieStore.addMovie(new Movie("Title2", 2015));
        movieStore.addMovie(new Movie("Title3", 2020));

        int id = movieStore.getAllMoviesList().getFirst().getId();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies/" + id))
                .DELETE()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        assertEquals(204, resp.statusCode(), "GET /movies должен вернуть 204");
        assertEquals(2, movieStore.getAllMoviesList().size());
    }

    @Test
    void deleteMovie_whenEmpty_returnsErrorMessage() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies/" + 100))
                .DELETE()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();

        assertEquals(404, resp.statusCode(), "GET /movies должен вернуть 404");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Фильм не найден", errorMessage);
    }

    @Test
    void deleteMovie_whenInvalidId_returnsErrorMessage() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + PORT + "/movies/udj"))
                .DELETE()
                .build();
        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String errorMessage = jsonObject.get("error").getAsString();

        assertEquals(400, resp.statusCode(), "GET /movies должен вернуть 400");
        assertTrue(jsonElement.isJsonObject(), "Ожидается JSON-объект");
        assertEquals("Некорректный ID", errorMessage);
    }
}