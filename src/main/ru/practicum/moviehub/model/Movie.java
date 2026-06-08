package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    private final String title;
    private final int year;
    private final int id;
    private static int nextId = 1;

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
        this.id = nextId + 1;
        nextId += 10;
    }

    public String getTitle() {
        return title;
    }

    public int getYear() {
        return year;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return year == movie.year && id == movie.id && Objects.equals(title, movie.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year, id);
    }
}