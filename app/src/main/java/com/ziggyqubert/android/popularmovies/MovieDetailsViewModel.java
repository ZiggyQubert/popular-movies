package com.ziggyqubert.android.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.annotation.NonNull;

import com.ziggyqubert.android.popularmovies.database.AppDatabase;
import com.ziggyqubert.android.popularmovies.database.FavoritesEntry;
import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.utilities.AppExecutors;

public class MovieDetailsViewModel extends AndroidViewModel {

    private Integer passedMovieId;
    private Boolean isFavorite;

    private AppDatabase mDb;

    private LiveData<FavoritesEntry> favoriteData;

    public MovieDetailsViewModel(@NonNull Application application) {
        super(application);
        passedMovieId = null;
        isFavorite = false;
        favoriteData = null;
        mDb = AppDatabase.getInstance(application.getApplicationContext());
    }

    public Integer getPassedMovieId() {
        return passedMovieId;
    }

    public void setPassedMovieId(Integer passedMovieId) {
        favoriteData = mDb.taskDao().loadFavoriteByMovieId(passedMovieId);
        this.passedMovieId = passedMovieId;
    }

    public LiveData<FavoritesEntry> getFavoriteData() {
        return favoriteData;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(final Boolean favorite, final Movie movieData) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (favorite) {
                    FavoritesEntry favoritesEntry = new FavoritesEntry(movieData);
                    mDb.taskDao().insertFavorite(favoritesEntry);
                } else {
                    mDb.taskDao().deleteFavoriteByMovieId(passedMovieId);
                }
            }
        });
        isFavorite = favorite;
    }
}
