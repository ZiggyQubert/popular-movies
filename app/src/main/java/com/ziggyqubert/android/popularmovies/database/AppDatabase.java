package com.ziggyqubert.android.popularmovies.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.util.Log;

import com.ziggyqubert.android.popularmovies.PopularMoviesApp;

@Database(entities = {FavoritesEntry.class}, version = 1, exportSchema = false)
@TypeConverters(DataConverters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "favorites";
    private static AppDatabase sInstance;

    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(PopularMoviesApp.APP_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        Log.d(PopularMoviesApp.APP_TAG, "Getting the database instance");
        return sInstance;
    }

    public abstract FavoritesDao taskDao();

}
