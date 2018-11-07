package com.ziggyqubert.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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

        //finds and sets up the poster image
        detailsBackgroundView = findViewById(R.id.detail_background);
        detailsPosterView = findViewById(R.id.detail_poster);

        detailsTaglineView = findViewById(R.id.detail_tagline);
        detailsYearView = findViewById(R.id.detail_year);
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
        setTitle(movieData.getTitle());

        //set the image
        Picasso.get()
                .load(movieData.getPosterUrl().toString())
                //error image taken from http://icons-for-free.com/
                .error(R.drawable.movie_not_found)
                .into(detailsPosterView);

        Picasso.get()
                .load(movieData.getBackdropUrl().toString())
                .into(detailsBackgroundView);

        detailsTaglineView.setText(movieData.getTagline());
        //display the release year
        Date releaseDate = movieData.getReleaseDate();
        String releaseYearString = new SimpleDateFormat("yyyy").format(releaseDate);
        detailsYearView.setText(releaseYearString);

        detailsLengthView.setText(movieData.getRuntime() + getString(R.string.minutesAbbr));
        detailsVotesView.setText(movieData.getVoteAverage() + "/10");

        detailsGenresView.setText("");
        List<String> genres = movieData.getGenres();
        Boolean first = true;
        for (String genre : genres) {
            if (!first) {
                detailsGenresView.append(", ");
            }
            detailsGenresView.append(genre);
            first = false;
        }

        detailsOverviewView.setText(movieData.getOverview());
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
