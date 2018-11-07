package com.ziggyqubert.android.popularmovies.model;

import android.util.Log;

import com.ziggyqubert.android.popularmovies.PopularMoviesApp;
import com.ziggyqubert.android.popularmovies.utilities.ThemoviedbUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Holds the model for a single movie
 */
public class Movie implements Serializable {

    //static properties used internally
    private static String IMAGE_BASE_URI = "http://image.tmdb.org/t/p/";
    private static String IMAGE_POSTER_SIZE_URI = "w185";
    private static String IMAGE_BACKDROP_SIZE_URI = "w780";

    //movie properties, dirived from the JSON returned by the service call
    private Integer id;

    private String title;
    private String origionalTitle;
    private String origionalLanguage;
    private String tagline;
    private Date releaseDate;
    private String overview;

    //urls used to load image data
    private URL posterUrl;
    private URL backdropUrl;

    private Boolean hasVideo;
    private Boolean isAdult;

    private Integer runtime;

    private Integer voteCount;
    private Double voteAverage;
    private Double popularity;

    private List<String> genres;

    /**
     * constructor to build the object out of a passed in json object
     *
     * @param jsonData
     */
    public Movie(JSONObject jsonData) {
        //sets the internal data properties based on the passed in JSON object
        try {
            id = jsonData.getInt("id");
            title = jsonData.getString("title");
            origionalTitle = jsonData.getString("original_title");
            origionalLanguage = jsonData.getString("original_language");
            if (jsonData.has("tagline")) {
                tagline = jsonData.getString("tagline");
            }

            String releaseDataText = jsonData.getString("release_date");
            SimpleDateFormat dateFormat = new SimpleDateFormat(ThemoviedbUtils.DATE_FORMAT);

            releaseDate = dateFormat.parse(releaseDataText);
            overview = jsonData.getString("overview");

            //builds the image urls
            String posterPath = jsonData.getString("poster_path");
            posterUrl = new URL(IMAGE_BASE_URI + IMAGE_POSTER_SIZE_URI + posterPath);
            String backdropPath = jsonData.getString("backdrop_path");
            backdropUrl = new URL(IMAGE_BASE_URI + IMAGE_BACKDROP_SIZE_URI + backdropPath);

            hasVideo = jsonData.getBoolean("video");
            isAdult = jsonData.getBoolean("adult");

            if (jsonData.has("runtime")) {
                runtime = jsonData.getInt("runtime");
            }

            voteCount = jsonData.getInt("vote_count");
            voteAverage = jsonData.getDouble("vote_average");
            popularity = jsonData.getDouble("popularity");

            //parses all the genre ids
            genres = new ArrayList<String>();
            if (jsonData.has("genres")) {
                JSONArray genreJsonArray = jsonData.getJSONArray("genres");
                for (int i = 0; i < genreJsonArray.length(); i++) {
                    JSONObject value = genreJsonArray.getJSONObject(i);
                    genres.add(value.getString("name"));
                }
            }

        } catch (JSONException e) {
            Log.e(PopularMoviesApp.APP_TAG, "Error parsing Movie JSON");
            e.printStackTrace();
        } catch (MalformedURLException e) {
            Log.e(PopularMoviesApp.APP_TAG, "Error creating URL");
            e.printStackTrace();
        } catch (ParseException e) {

        }
    }

    public Integer getId() {
        return id;
    }

    /**
     * gets the title
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public URL getBackdropUrl() {
        return backdropUrl;
    }

    /**
     * Gets the poster URL
     *
     * @return
     */
    public URL getPosterUrl() {
        return posterUrl;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public List<String> getGenres() {
        return genres;
    }
}
