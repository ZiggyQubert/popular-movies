package com.ziggyqubert.android.popularmovies.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.ziggyqubert.android.popularmovies.model.Movie;

import java.net.URL;
import java.util.Date;

@Entity(tableName = "favorites")
public class FavoritesEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private Integer movieId;
    private String title;
    private URL posterUrl;
    @ColumnInfo(name = "added_at")
    private Date addedAt;

    @Ignore
    public FavoritesEntry(Integer movieId, String title, URL posterUrl) {
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.addedAt = new Date();
    }

    @Ignore
    public FavoritesEntry(Movie movie) {
        this.movieId = movie.getId();
        this.title = movie.getTitle();
        this.posterUrl = movie.getPosterUrl();
        this.addedAt = new Date();
    }

    public FavoritesEntry(int id, Integer movieId, String title, URL posterUrl) {
        this.id = id;
        this.movieId = movieId;
        this.title = title;
        this.posterUrl = posterUrl;
        this.addedAt = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public URL getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(URL posterUrl) {
        this.posterUrl = posterUrl;
    }

    public Date getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(Date addedAt) {
        this.addedAt = addedAt;
    }

    public Movie getMovieObject() {
        return new Movie(this.movieId, this.title, this.posterUrl);
    }
}
