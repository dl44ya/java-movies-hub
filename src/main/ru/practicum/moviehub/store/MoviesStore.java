package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class MoviesStore {
    private Map<Integer, Movie> moviesStore;

    public MoviesStore() {
        this.moviesStore = new HashMap<>();
    }

    public void addMovie(Movie movie) {
        moviesStore.put(movie.getId(), movie);
    }

    public Movie getMovieById(int id) {
        return moviesStore.get(id);
    }

    public List<Movie> getMoviesByYear(int year) {
        List<Movie> movies = moviesStore.values().stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
        return movies;
    }

    public void deleteMovie(int id) {
        moviesStore.remove(id);
    }

    public void clearMoviesStore() {
        moviesStore.clear();
    }

    public List<Movie> getAllMoviesList() {
        List<Movie> movies = moviesStore.values().stream().collect(Collectors.toList());
        return movies;
    }

    public Map<Integer, Movie> getMoviesStore() {
        return moviesStore;
    }

    @Override
    public String toString() {
        return "MoviesStore{" +
                "moviesStore=" + moviesStore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MoviesStore store = (MoviesStore) o;
        return Objects.equals(moviesStore, store.moviesStore);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(moviesStore);
    }
}