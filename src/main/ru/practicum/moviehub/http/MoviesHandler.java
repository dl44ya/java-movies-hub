package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore moviesStore;
    private final Gson gson;
    private final String errorMessage_404 = "Фильм не найден";
    private final String errorMessage_400_Id = "Некорректный ID";
    private final String errorMessage_400_Year = "Некорректный параметр запроса — year";
    private final String errorMessage_405 = "Неподдерживаемый метод";
    private final String errorMessage_422 = "Ошибка валидации";
    private final String titleErrorDetails = "Название не должно быть пустым или длинее 100 символов";
    private final String yearErrorDetails = "Год должен быть между 1888 и 2026";
    private final String errorMessage_415 = "Неподдерживаемый тип данных";


    public MoviesHandler(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
        gson = new Gson();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Endpoint endpoint = getEndpoint(ex.getRequestURI(), ex.getRequestMethod());

        switch (endpoint) {
            case GET_MOVIES:
                handleGetMovies(ex);
                break;
            case GET_MOVIE_BY_ID:
                handleGetMoviesById(ex);
                break;
            case GET_MOVIES_BY_YEAR:
                handleGetMoviesByYear(ex);
                break;
            case POST:
                handlePostMovie(ex);
                break;
            case DELETE:
                handleDeleteMovie(ex);
                break;
            default:
                sendErrorJson(ex, 405, gson.toJson(new ErrorResponse(errorMessage_405)));
        }
    }

    private void handleGetMovies(HttpExchange ex) throws IOException {
        sendJson(ex, 200, gson.toJson(moviesStore.getAllMoviesList()));
    }

    private void handleGetMoviesById(HttpExchange ex) throws IOException {
        String[] path = ex.getRequestURI().getPath().split("/");
        try {
            int id = Integer.parseInt(path[2]);
            Movie movie = moviesStore.getMovieById(id);
            if (movie == null) {
                sendErrorJson(ex, 404, gson.toJson(new ErrorResponse(errorMessage_404)));
            } else {
                sendJson(ex, 200, gson.toJson(moviesStore.getMovieById(id)));
            }
        } catch (NumberFormatException e) {
            sendErrorJson(ex, 400, gson.toJson(new ErrorResponse(errorMessage_400_Id)));
        }
    }

    private void handleGetMoviesByYear(HttpExchange ex) throws IOException {
        String query = ex.getRequestURI().getQuery();
        if (query == null) {
            sendErrorJson(ex, 400, gson.toJson(new ErrorResponse(errorMessage_400_Year)));
        } else {
            try {
                String[] queryParts = query.split("=");
                int year = Integer.parseInt(queryParts[1]);
                List<Movie> movies = moviesStore.getMoviesByYear(year);
                sendJson(ex, 200, gson.toJson(movies));
            } catch (NumberFormatException e) {
                sendErrorJson(ex, 400, gson.toJson(new ErrorResponse(errorMessage_400_Year)));
            }
        }
    }

    private void handlePostMovie(HttpExchange ex) throws IOException {
        InputStream inputStream = ex.getRequestBody();
        List<String> contentTypeValues = ex.getRequestHeaders().get("Content-type");

        if (contentTypeValues == null || !contentTypeValues.contains("application/json; charset=UTF-8")) {
            sendErrorJson(ex, 415, gson.toJson(new ErrorResponse(errorMessage_415)));
        } else {
            JsonElement jsonElement = JsonParser.parseString(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            if (!jsonObject.has("title") || !jsonObject.has("year") || jsonObject.isEmpty()) {
                sendErrorJson(ex, 415, gson.toJson(new ErrorResponse(errorMessage_415)));
            } else {
                String title = jsonObject.get("title").getAsString();
                int year = jsonObject.get("year").getAsInt();

                if (title == null || title.trim().isEmpty() || title.length() > 100) {
                    sendErrorJson(ex, 422, gson.toJson(
                            new ErrorResponse(errorMessage_422, new String[]{titleErrorDetails})));
                    return;
                }
                if (year < 1888 || year > 2027) {
                    sendErrorJson(ex, 422, gson.toJson(
                            new ErrorResponse(errorMessage_422, new String[]{yearErrorDetails})));
                    return;
                }

                Movie newMovie = new Movie(title, year);
                moviesStore.addMovie(newMovie);
                sendJson(ex, 201, gson.toJson(newMovie));
            }
        }
    }


    private void handleDeleteMovie(HttpExchange ex) throws IOException {
        String[] pathParts = ex.getRequestURI().getPath().split("/");
        try {
            int id = Integer.parseInt(pathParts[2]);
            if (moviesStore.getMoviesStore().get(id) == null) {
                sendErrorJson(ex, 404, gson.toJson(new ErrorResponse(errorMessage_404)));
            } else {
                moviesStore.deleteMovie(id);
                sendNoContent(ex);
            }
        } catch (NumberFormatException e) {
            sendErrorJson(ex, 400, gson.toJson(new ErrorResponse(errorMessage_400_Id)));
        }
    }

    private Endpoint getEndpoint(URI url, String requestMethod) {
        String path = url.getPath();
        String query = url.getQuery();
        String[] pathParts = path.split("/");

        if (requestMethod.equals("GET")) {
            if (query != null && query.startsWith("year=")) {
                return Endpoint.GET_MOVIES_BY_YEAR;
            }
            if (pathParts.length == 2 && path.equals("/movies") && query == null) {
                return Endpoint.GET_MOVIES;
            }
            if (pathParts.length == 3) {
                return Endpoint.GET_MOVIE_BY_ID;
            }
        }
        if (requestMethod.equals("DELETE")) {
            return Endpoint.DELETE;
        }
        if (requestMethod.equals("POST")) {
            return Endpoint.POST;
        }
        return Endpoint.UNKNOWN;
    }
}
