package com.ziggyqubert.android.popularmovies.utilities;

import android.util.Log;

import com.ziggyqubert.android.popularmovies.PopularMoviesApp;
import com.ziggyqubert.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataParsers {

    /**
     * takes an array of json  and builds the movie list
     *
     * @param movieListJsonArray
     * @return
     */
    public static List<Movie> parseMovieList(JSONArray movieListJsonArray) {

        List<Movie> movieList = new ArrayList<Movie>();

        //if data is passed
        if (movieListJsonArray != null) {
            //create each movie object from the array list
            for (int i = 0; i < movieListJsonArray.length(); i++) {
                try {
                    JSONObject value = movieListJsonArray.getJSONObject(i);
                    movieList.add(new Movie(value));
                } catch (JSONException e) {
                    Log.e(PopularMoviesApp.APP_TAG, "Error parsing movie JSON object");
                    e.printStackTrace();
                }
            }
        }

        return movieList;
    }

    /**
     * parses the movie list from the raw json response
     * @param jsonResponseData
     * @return
     */
    public static List<Movie> parseMovieList(JSONObject jsonResponseData) {

        JSONArray movieList = null;
        try {
            movieList = jsonResponseData.getJSONArray("results");
        } catch (JSONException e) {
            Log.e(PopularMoviesApp.APP_TAG, "Error parsing list of movies");
        }

        return parseMovieList(movieList);
    }
}