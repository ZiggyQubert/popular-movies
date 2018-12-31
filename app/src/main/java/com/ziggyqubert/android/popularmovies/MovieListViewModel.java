package com.ziggyqubert.android.popularmovies;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.utilities.ThemoviedbUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieListViewModel extends AndroidViewModel {

    public static String SHARED_PREF_SORTBY_KEY = "sortByPref";

    private String sortType;
    private Integer currentPage;
    private List<Movie> movieList;

    SharedPreferences sharedPreferences;

    public MovieListViewModel(Application application) {
        super(application);
        Log.i(PopularMoviesApp.APP_TAG, "Create MovieListViewModel");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application.getApplicationContext());

        sortType = sharedPreferences.getString(SHARED_PREF_SORTBY_KEY, ThemoviedbUtils.SORT_MOST_POPULAR);
        currentPage = 0;
        movieList = new ArrayList<Movie>();

    }

    public String getSortType() {
        return sortType;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREF_SORTBY_KEY, sortType);
        editor.commit();
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void resetCurrentPage() {
        setCurrentPage(0);
    }

    public void incrimentCurrentPage() {
        setCurrentPage(currentPage + 1);
    }


    public List<Movie> getMovieList() {
        return movieList;
    }

    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
    }

}
