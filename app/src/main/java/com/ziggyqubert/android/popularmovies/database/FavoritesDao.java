package com.ziggyqubert.android.popularmovies.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface FavoritesDao {

    @Query("SELECT * FROM favorites ORDER BY id")
    LiveData<List<FavoritesEntry>> loadAllFavorites();

    @Insert
    void insertFavorite(FavoritesEntry favoritesEntry);

    @Delete
    void deleteFavorite(FavoritesEntry favoritesEntry);

    @Query("DELETE FROM favorites WHERE movieId = :id")
    void deleteFavoriteByMovieId(Integer id);

    @Query("SELECT * FROM favorites WHERE movieId = :id")
    LiveData<FavoritesEntry> loadFavoriteByMovieId(Integer id);
}
