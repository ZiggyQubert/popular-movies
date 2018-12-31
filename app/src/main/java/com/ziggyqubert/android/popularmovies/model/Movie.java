package com.ziggyqubert.android.popularmovies.model;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.ziggyqubert.android.popularmovies.PopularMoviesApp;
import com.ziggyqubert.android.popularmovies.utilities.DataParsers;
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
 * Holds the model for a single movie, note removing Serializable as its not actualy being used
 */
public class Movie {

    //static properties used internally
    private static String IMAGE_BASE_URI = "http://image.tmdb.org/t/p/";
    private static String IMAGE_POSTER_SIZE_URI = "w185";
    private static String IMAGE_BACKDROP_SIZE_URI = "w780";
    private static String RELEASE_COUNTRY_CODE = "US";

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

    private String mpaaRating;

    private List<String> genres;

    private List<MoviePreview> videos;

    private List<MovieReview> reviews;

    /**
     * constructor to build the object out of a passed in json object
     *
     * @param jsonData
     */
    public Movie(JSONObject jsonData) {
        //sets the internal data properties based on the passed in JSON object
        id = DataParsers.safeGetIntFromJson(jsonData, "id");

        title = DataParsers.safeGetStringFromJson(jsonData, "title");
        origionalTitle = DataParsers.safeGetStringFromJson(jsonData, "original_title");
        origionalLanguage = DataParsers.safeGetStringFromJson(jsonData, "original_language");
        tagline = DataParsers.safeGetStringFromJson(jsonData, "tagline");

        String releaseDataText = DataParsers.safeGetStringFromJson(jsonData, "release_date");
        if (releaseDataText != "") {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(ThemoviedbUtils.DATE_FORMAT);
                releaseDate = dateFormat.parse(releaseDataText);
            } catch (ParseException e) {
                Log.e(PopularMoviesApp.APP_TAG, "Error parsing date");
                e.printStackTrace();
            }
        }
        overview = DataParsers.safeGetStringFromJson(jsonData, "overview");

        //builds the image urls
        try {
            String posterPath = DataParsers.safeGetStringFromJson(jsonData, "poster_path");
            posterUrl = new URL(IMAGE_BASE_URI + IMAGE_POSTER_SIZE_URI + posterPath);
            String backdropPath = DataParsers.safeGetStringFromJson(jsonData, "backdrop_path");
            backdropUrl = new URL(IMAGE_BASE_URI + IMAGE_BACKDROP_SIZE_URI + backdropPath);
        } catch (MalformedURLException e) {
            Log.e(PopularMoviesApp.APP_TAG, "Error creating URL");
            e.printStackTrace();
        }

        hasVideo = DataParsers.safeGetBoolFromJson(jsonData, "video");
        isAdult = DataParsers.safeGetBoolFromJson(jsonData, "adult");
        runtime = DataParsers.safeGetIntFromJson(jsonData, "runtime");
        voteCount = DataParsers.safeGetIntFromJson(jsonData, "vote_count");
        voteAverage = DataParsers.safeGetDoubleFromJson(jsonData, "vote_average");
        popularity = DataParsers.safeGetDoubleFromJson(jsonData, "popularity");

        genres = DataParsers.safeGetStringArrayFromJson(jsonData, "genres", "name");

        mpaaRating = DataParsers.safeGetRatingString(jsonData, RELEASE_COUNTRY_CODE);

        videos = DataParsers.safeGetVideosFromJson(jsonData, "videos");
        reviews = DataParsers.safeGetReviewsFromJson(jsonData, "reviews");

    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTagline() {
        return tagline;
    }

    public URL getBackdropUrl() {
        return backdropUrl;
    }

    public URL getPosterUrl() {
        return posterUrl;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public Integer getReleaseYear() {
        String releaseYearString = new SimpleDateFormat("yyyy").format(releaseDate);
        return Integer.parseInt(releaseYearString);
    }

    public String getOverview() {
        return overview;
    }

    public String getRuntime() {
        if (runtime != null && runtime > 0) {
            return runtime + "min";
        } else {
            return "";
        }
    }

    public String getVoteAverage() {
        if (voteAverage != null && voteAverage > 0) {
            return voteAverage + "/10";
        } else {
            return "";
        }
    }

    public String getGenres() {
        return TextUtils.join(", ", genres);
    }

    public String getMpaaRating() {
        return mpaaRating;
    }

    public List<MoviePreview> getVideos() {
        return videos;
    }
}
