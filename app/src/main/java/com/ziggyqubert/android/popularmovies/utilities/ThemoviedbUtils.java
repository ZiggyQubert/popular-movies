package com.ziggyqubert.android.popularmovies.utilities;

import android.net.Uri;
import android.util.Log;

import com.ziggyqubert.android.popularmovies.BuildConfig;
import com.ziggyqubert.android.popularmovies.PopularMoviesApp;
import com.ziggyqubert.android.popularmovies.model.Movie;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ThemoviedbUtils {

    public static final String SORT_TOP_RATED = "top_rated";
    public static final String SORT_MOST_POPULAR = "popular";
    public static final String SORT_NOW_PLAYING = "now_playing";
    public static final String SORT_UPCOMING = "upcoming";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    //the api query paramater
    private static final String THEMOVIEDB_APIKEY_PARAM = "api_key";
    //holds the API key read in from the build config
    private static final String THEMOVIEDB_APIKEY_VALUE = BuildConfig.THEMOVIEDB_V3_APIKEY;

    private static final String THEMOVIEDB_LANGUAGE_PARAM = "language";
    private static final String THEMOVIEDB_LANGUAGE_VALUE = "en";

    //the page paramater name
    private static final String THEMOVIEDB_PAGE_PARAM = "page";
    //base url for the api
    public static final String THEMOVIEDB_API_BASE_URL = "http://api.themoviedb.org";

    /**
     * Generic method to make a request, will append the API key to all requests
     *
     * @param requestUrl
     * @return
     */
    private static JSONObject makeRequest(Uri requestUrl) {

        //adds the api key to the request
        Uri builtUri = Uri.parse(requestUrl.toString()).buildUpon()
                .appendQueryParameter(THEMOVIEDB_LANGUAGE_PARAM, THEMOVIEDB_LANGUAGE_VALUE)
                .appendQueryParameter(THEMOVIEDB_APIKEY_PARAM, THEMOVIEDB_APIKEY_VALUE)
                .build();

        //builds the url from the uri
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            Log.e(PopularMoviesApp.APP_TAG, "Error creating url");
            e.printStackTrace();
        }

        Log.i(PopularMoviesApp.APP_TAG, "Request Start: " + url.toString());

        //makes and gets the response
        String requestResponse = "";
        try {
            requestResponse = getResponseFromHttpUrl(url);
        } catch (IOException e) {
            Log.e(PopularMoviesApp.APP_TAG, "Error fetching url");
            e.printStackTrace();
        }

        //converts the response to a json object
        JSONObject jsonResponse = null;
        if (requestResponse != null) {
            try {
                jsonResponse = new JSONObject(requestResponse);
            } catch (JSONException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing JSON");
                e.printStackTrace();
            }

            Log.i(PopularMoviesApp.APP_TAG, "Request Complete: " + url.toString());
            if (jsonResponse != null) {
                //logs out the response
                try {
                    Log.i(PopularMoviesApp.APP_TAG, jsonResponse.toString(2));
                } catch (JSONException e) {
                    Log.i(PopularMoviesApp.APP_TAG, "Failed to stringify JSON");
                }
            } else {
                Log.i(PopularMoviesApp.APP_TAG, "-- NO RESPONSE BODY --");
            }
        }
        return jsonResponse;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);

        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } catch (SocketTimeoutException e) {
            return null;
        } finally {
            urlConnection.disconnect();
        }
    }

    /**
     * spicific function to request the popular Movie list, gets page 1
     *
     * @return
     */
    public static List<Movie> fetchPopularMovieList() {
        //gets the first page of data
        return fetchPopularMovieList(1);
    }

    /**
     * spicific function to get popular movies by page number
     *
     * @param pageNumber
     * @return
     */
    public static List<Movie> fetchPopularMovieList(Integer pageNumber) {
        return fetchMovieList(SORT_MOST_POPULAR, pageNumber);
    }

    /**
     * spicific function to request the top rated Movie list, gets page 1
     *
     * @return
     */
    public static List<Movie> fetchTopRatedMovieList() {
        return fetchTopRatedMovieList(1);
    }

    /**
     * spicific function to get top rated movies by page number
     *
     * @param pageNumber
     * @return
     */
    public static List<Movie> fetchTopRatedMovieList(Integer pageNumber) {
        return fetchMovieList(SORT_TOP_RATED, pageNumber);
    }

    /**
     * generic function to fetch movies by page number, with sorting
     *
     * @param sortBy
     * @param pageNumber
     * @return
     */
    public static List<Movie> fetchMovieList(String sortBy, Integer pageNumber) {
        Log.i(PopularMoviesApp.APP_TAG, "fetchMovieList by: " + sortBy + " pg: " + pageNumber);
        Uri fetchMovieListUri = Uri.parse(THEMOVIEDB_API_BASE_URL + "/3/movie/" + sortBy);

        if (pageNumber < 1) {
            Log.w(PopularMoviesApp.APP_TAG, "Page number too small, returning no results");
            return new ArrayList<Movie>();
        } else if (pageNumber > 1) {
            fetchMovieListUri = fetchMovieListUri.buildUpon()
                    .appendQueryParameter(THEMOVIEDB_PAGE_PARAM, pageNumber.toString())
                    .build();
        }

        JSONObject responseJson = makeRequest(fetchMovieListUri);

        if (responseJson == null) {
            return null;
        } else {
            return DataParsers.parseMovieList(responseJson);
        }
    }

    /**
     * gets details for a spicific movie
     *
     * @param movieId
     * @return
     */
    public static Movie fetchMovieDetails(Integer movieId) {
        Log.i(PopularMoviesApp.APP_TAG, "fetchMovieDetails for: " + movieId);
        Uri fetchMovieListUri = Uri.parse(THEMOVIEDB_API_BASE_URL + "/3/movie/" + movieId)
                .buildUpon()
                .appendQueryParameter("append_to_response", "release_dates")
                .build();

        JSONObject responseJson = makeRequest(fetchMovieListUri);

        if (responseJson == null) {
            return null;
        } else {
            return new Movie(responseJson);
        }
    }
}
