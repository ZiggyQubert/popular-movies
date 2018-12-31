package com.ziggyqubert.android.popularmovies.utilities;

import android.util.Log;

import com.ziggyqubert.android.popularmovies.PopularMoviesApp;
import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.model.MoviePreview;
import com.ziggyqubert.android.popularmovies.model.MovieReview;

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

    /**
     * gets a list of strings from json
     *
     * @param jsonData
     * @param propertyName
     * @param subProperty
     * @return
     */
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
     * gets the rating for a particular country code, note this gets the rating for the first release regardless of what type of release it was, see https://developers.themoviedb.org/3/movies/get-movie-release-dates for exact specs on the release property
     *
     * @param jsonData
     * @param countryCode
     * @return
     */
    public static String safeGetRatingString(JSONObject jsonData, String countryCode) {
        String rating = null;
        if (jsonData.has("release_dates")) {
            try {
                JSONObject releaseData = jsonData.getJSONObject("release_dates");
                if (releaseData.has("results")) {
                    JSONArray releasesList = releaseData.getJSONArray("results");

                    for (int i = 0; i < releasesList.length(); i++) {
                        JSONObject releasesForCountry = releasesList.getJSONObject(i);
                        if (safeGetStringFromJson(releasesForCountry, "iso_3166_1").equalsIgnoreCase(countryCode)) {
                            JSONArray releaseDates = releasesForCountry.getJSONArray("release_dates");
                            rating = safeGetStringFromJson(releaseDates.getJSONObject(0), "certification");
                            break;
                        }
                    }

                }
            } catch (JSONException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing rating for " + countryCode);
                e.printStackTrace();
            }
        }
        return rating;
    }

    public static List<MovieReview> safeGetReviewsFromJson(JSONObject jsonData, String propertyName) {
        List<MovieReview> reviewList = new ArrayList<MovieReview>();

        JSONArray jsonReviewValues = null;
        if (jsonData.has(propertyName)) {
            try {
                if (jsonData.getJSONObject(propertyName).has("results")) {
                    jsonReviewValues = jsonData.getJSONObject(propertyName).getJSONArray("results");
                } else {
                    jsonReviewValues = jsonData.getJSONArray(propertyName);
                }
            } catch (JSONException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing JSON");
                e.printStackTrace();
            }
        }


        if (jsonReviewValues != null) {
            for (int i = 0; i < jsonReviewValues.length(); i++) {
                try {
                    JSONObject reviewData = jsonReviewValues.getJSONObject(i);
                    MovieReview review = new MovieReview(reviewData);
                    reviewList.add(review);
                } catch (JSONException e) {
                    Log.e(PopularMoviesApp.APP_TAG, "Error parsing JSON");
                    e.printStackTrace();
                }
            }
        }

        return reviewList;
    }

    public static List<MoviePreview> safeGetVideosFromJson(JSONObject jsonData, String propertyName) {
        List<MoviePreview> videoList = new ArrayList<MoviePreview>();

        JSONArray jsonVideoValues = null;
        if (jsonData.has(propertyName)) {
            try {
                if (jsonData.getJSONObject(propertyName).has("results")) {
                    jsonVideoValues = jsonData.getJSONObject(propertyName).getJSONArray("results");
                } else {
                    jsonVideoValues = jsonData.getJSONArray(propertyName);
                }
            } catch (JSONException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing JSON");
                e.printStackTrace();
            }
        }

        if (jsonVideoValues != null) {
            for (int i = 0; i < jsonVideoValues.length(); i++) {
                try {
                    JSONObject videoData = jsonVideoValues.getJSONObject(i);
                    MoviePreview preview = new MoviePreview(videoData);
                    videoList.add(preview);
                } catch (JSONException e) {
                    Log.e(PopularMoviesApp.APP_TAG, "Error parsing JSON");
                    e.printStackTrace();
                }
            }
        }
        return videoList;
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