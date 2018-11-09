package com.ziggyqubert.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.utilities.ThemoviedbUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MovieDetails extends AppCompatActivity {

    Movie selectedMovieData;

    ImageView detailsBackgroundView;

    ImageView detailsPosterView;
    TextView detailsTaglineView;
    TextView detailsYearView;
    TextView detailsRatingView;
    TextView detailsLengthView;
    TextView detailsVotesView;
    TextView detailsGenresView;
    TextView detailsOverviewView;

    ProgressBar detailsLoadingSpinner;
    ScrollView detailsMovieContnetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        detailsBackgroundView = findViewById(R.id.detail_background);
        detailsPosterView = findViewById(R.id.detail_poster);

        detailsTaglineView = findViewById(R.id.detail_tagline);
        detailsYearView = findViewById(R.id.detail_year);
        detailsRatingView = findViewById(R.id.detail_rating);
        detailsVotesView = findViewById(R.id.detail_votes);
        detailsLengthView = findViewById(R.id.detail_length);
        detailsGenresView = findViewById(R.id.detail_genres);

        detailsOverviewView = findViewById(R.id.detail_overview);

        detailsLoadingSpinner = findViewById(R.id.detail_loading_spinner);
        detailsMovieContnetView = findViewById(R.id.detail_movie_content);

        Intent intentThatStartedThisActivity = getIntent();

        //get the movie data from the intent
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_UID)) {
            Integer passedMovieId = intentThatStartedThisActivity.getIntExtra(Intent.EXTRA_UID, 0);
            if (passedMovieId > 0) {
                new FetchMovieDetails().execute(passedMovieId);
            } else {
                showMovieNotLoaded();
            }
        } else {
            showMovieNotLoaded();
        }
    }

    /**
     * basic error handeling for when movie data is not found
     */
    protected void showMovieNotLoaded() {
        Toast.makeText(this, getText(R.string.noMovieError), Toast.LENGTH_SHORT).show();
        this.finish();
    }

    /**
     * sets the movie data to display and populates the UI
     *
     * @param movieData
     */
    protected void setMovieInformation(Movie movieData) {
        selectedMovieData = movieData;

        String movieTitle = movieData.getTitle().trim();
        setTitle(movieTitle);

        //set the poster
        Picasso.get()
                .load(movieData.getPosterUrl().toString())
                //error image taken from http://icons-for-free.com/
                .error(R.drawable.movie_not_found_details)
                .into(detailsPosterView);

        //sets the background image
        Picasso.get()
                .load(movieData.getBackdropUrl().toString())
                .into(detailsBackgroundView);

        //sets all the movie information

        //if the tagline is the same as the title don't show the tagline, removes all non standard alpha numeric characters for the comparison
        String movieTagline = movieData.getTagline().trim();
        String titleForCompare = movieTitle.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        String taglineForCompare = movieTagline.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        String displayMovieTagline = titleForCompare.equalsIgnoreCase(taglineForCompare) ? "" : movieTagline;
        displayInView(detailsTaglineView, displayMovieTagline);

        displayInView(detailsYearView, movieData.getReleaseYear());
        displayInView(detailsRatingView, movieData.getMpaaRating());

        displayInView(detailsLengthView, movieData.getRuntime());
        displayInView(detailsVotesView, movieData.getVoteAverage());

        displayInView(detailsGenresView, movieData.getGenres());

        displayInView(detailsOverviewView, movieData.getOverview());
    }

    /**
     * handles displaying string values in the view and showing or hiding the view as necessary
     *
     * @param view
     * @param newValue
     */
    private void displayInView(TextView view, String newValue) {
        if (newValue != null && !newValue.trim().equals("")) {
            view.setText(newValue);
            view.setVisibility(View.VISIBLE);
        } else {
            view.setText("");
            view.setVisibility(View.GONE);
        }
    }

    /**
     * handles displaying int values in the view and showing or hiding the view as necessary
     *
     * @param view
     * @param newValue
     */
    private void displayInView(TextView view, Integer newValue) {
        displayInView(view, newValue.toString());
    }

    /**
     * handles displaying double values in the view and showing or hiding the view as necessary
     *
     * @param view
     * @param newValue
     */
    private void displayInView(TextView view, Double newValue) {
        displayInView(view, newValue.toString());
    }


    /**
     * Async task to load the popular movies data, takes the page of movie data to load, returns a list of movies
     */
    public class FetchMovieDetails extends AsyncTask<Integer, Void, Movie> {

        /**
         * shows the load spinner
         */
        @Override
        protected void onPreExecute() {
            detailsLoadingSpinner.setVisibility(View.VISIBLE);
            detailsMovieContnetView.setVisibility(View.INVISIBLE);
            detailsBackgroundView.setVisibility(View.INVISIBLE);
            super.onPreExecute();
        }

        /**
         * runs the query to fetch data
         *
         * @param movieIds
         * @return
         */
        @Override
        protected Movie doInBackground(Integer... movieIds) {
            Integer movieIdToLoad = movieIds[0];
            Movie movieData;
            //fetching the movie details here as we need more information on the movie then we get in the list call
            movieData = ThemoviedbUtils.fetchMovieDetails(movieIdToLoad);
            return movieData;
        }

        /**
         * handles the response
         *
         * @param movieData
         */
        @Override
        protected void onPostExecute(Movie movieData) {

            // hides the loading / progress indicators
            detailsLoadingSpinner.setVisibility(View.GONE);
            detailsMovieContnetView.setVisibility(View.VISIBLE);
            detailsBackgroundView.setVisibility(View.VISIBLE);

            //if data was loaded add it to the adapter
            if (movieData != null) {
                setMovieInformation(movieData);
            } else {
                showMovieNotLoaded();
            }
        }
    }
}
