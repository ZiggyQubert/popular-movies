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
     *
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

    /**
     * safely gets a string from json
     *
     * @param jsonData
     * @param propertyName
     * @return
     */
    public static String safeGetStringFromJson(JSONObject jsonData, String propertyName) {
        return safeGetFromJson(jsonData, propertyName, "").toString();
    }

    /**
     * safely gets an Int from json
     *
     * @param jsonData
     * @param propertyName
     * @return
     */
    public static Integer safeGetIntFromJson(JSONObject jsonData, String propertyName) {
        Object numberProperty = safeGetFromJson(jsonData, propertyName, null);
        try {
            return Integer.parseInt(numberProperty.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * safely gets a double from json
     *
     * @param jsonData
     * @param propertyName
     * @return
     */
    public static Double safeGetDoubleFromJson(JSONObject jsonData, String propertyName) {
        Object numberProperty = safeGetFromJson(jsonData, propertyName, null);
        try {
            return Double.parseDouble(numberProperty.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * safely gets a bool from json
     *
     * @param jsonData
     * @param propertyName
     * @return
     */
    public static Boolean safeGetBoolFromJson(JSONObject jsonData, String propertyName) {
        Object boolVal = safeGetFromJson(jsonData, propertyName, false);
        try {
            if ((Boolean) boolVal == true) {
                return true;
            } else {
                return false;
            }
        } catch (Error e) {
            return false;
        }
    }

    public static List<String> safeGetStringArrayFromJson(JSONObject jsonData, String propertyName, String subProperty) {
        List<String> stringArray = new ArrayList<String>();
        if (jsonData.has(propertyName)) {
            try {
                //parses all the genre ids
                JSONArray genreJsonArray = jsonData.getJSONArray("genres");
                for (int i = 0; i < genreJsonArray.length(); i++) {
                    JSONObject value = genreJsonArray.getJSONObject(i);
                    if (value.has(subProperty)) {
                        stringArray.add(safeGetStringFromJson(value, subProperty));
                    }
                }
            } catch (JSONException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing stringArray " + propertyName);
                e.printStackTrace();
            }
        }
        return stringArray;
    }

    /**
     * function to get generic data value from json
     *
     * @param jsonData
     * @param propertyName
     * @param defaultValue
     * @return
     */
    public static Object safeGetFromJson(JSONObject jsonData, String propertyName, Object defaultValue) {
        Object propValue = null;
        if (jsonData.has(propertyName)) {
            try {
                propValue = jsonData.get(propertyName);
            } catch (JSONException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing JSON");
                e.printStackTrace();
            }
        }
        if (propValue == null) {
            propValue = defaultValue;
        }
        return propValue;
    }
}