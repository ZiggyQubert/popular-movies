package com.ziggyqubert.android.popularmovies;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.ziggyqubert.android.popularmovies.databinding.ActivityMovieDetailsBinding;
import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.model.MoviePreview;
import com.ziggyqubert.android.popularmovies.utilities.MovieDetailsAsyncLoader;

import java.util.List;

public class MovieDetails extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Movie>, TrailerAdapter.TrailerAdapterOnClickHandler {

    Integer MOVIE_DETAILS_LOADER_ID = 1;

    Movie selectedMovieData;

    ProgressBar detailsLoadingSpinner;
    View detailsMovieContnetView;

    Integer passedMovieId;

    ActivityMovieDetailsBinding mBinding;

    private RecyclerView trailerRecyclerView;
    private TrailerAdapter trailerAdapter;

    /**
     * gets the movie id that was passed to the details screen
     *
     * @return
     */
    public Integer getPassedMovieId() {
        return passedMovieId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_details);

        detailsLoadingSpinner = findViewById(R.id.detail_loading_spinner);
        detailsMovieContnetView = findViewById(R.id.detail_movie_content);


        //sets up the recycler view
        trailerRecyclerView = findViewById(R.id.trailer_display_recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        trailerRecyclerView.setLayoutManager(layoutManager);
        trailerAdapter = new TrailerAdapter(this);
        trailerRecyclerView.setAdapter(trailerAdapter);

        Intent intentThatStartedThisActivity = getIntent();

        //get the movie data from the intent
        if (intentThatStartedThisActivity.hasExtra(Intent.EXTRA_UID)) {
            passedMovieId = intentThatStartedThisActivity.getIntExtra(Intent.EXTRA_UID, 0);
            if (passedMovieId > 0) {

                //instance state is saved, but curently not doing anything, movie data is cached in the loader so no need to double cache it in the instance state
                if (savedInstanceState != null) {
                    Log.i(PopularMoviesApp.APP_TAG, "Load activity from saved instance state");
                } else {
                    Log.i(PopularMoviesApp.APP_TAG, "Load activity with defaults");
                }

                LoaderManager.LoaderCallbacks<Movie> callback = MovieDetails.this;
                Bundle bundleForLoader = null;
                detailsLoadingSpinner.setVisibility(View.VISIBLE);
                detailsMovieContnetView.setVisibility(View.INVISIBLE);
                mBinding.detailBackground.setVisibility(View.INVISIBLE);
                getLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, bundleForLoader, callback);
            } else {
                showMovieNotLoaded();
            }
        } else {
            showMovieNotLoaded();
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(PopularMoviesApp.APP_TAG, "onSaveInstanceState");
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
                .into(mBinding.content.detailPoster);

        //sets the background image
        Picasso.get()
                .load(movieData.getBackdropUrl().toString())
                .into(mBinding.detailBackground);

        //sets all the movie information

        //if the tagline is the same as the title don't show the tagline, removes all non standard alpha numeric characters for the comparison
        String movieTagline = movieData.getTagline().trim();
        String titleForCompare = movieTitle.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        String taglineForCompare = movieTagline.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        String displayMovieTagline = titleForCompare.equalsIgnoreCase(taglineForCompare) ? "" : movieTagline;

        displayInView(mBinding.content.detailTagline, displayMovieTagline);

        displayInView(mBinding.content.detailYear, movieData.getReleaseYear());
        displayInView(mBinding.content.detailRating, movieData.getMpaaRating());

        displayInView(mBinding.content.detailLength, movieData.getRuntime());
        displayInView(mBinding.content.detailVotes, movieData.getVoteAverage());

        displayInView(mBinding.content.detailGenres, movieData.getGenres());

        displayInView(mBinding.content.detailOverview, movieData.getOverview());

        List<MoviePreview> trailerData = movieData.getVideos();
        View trailerArea = findViewById(R.id.detail_trailers);
        if (trailerData.size() > 0) {
            trailerArea.setVisibility(View.VISIBLE);
            trailerAdapter.setTrailerData(movieData.getVideos());
        } else {
            trailerArea.setVisibility(View.GONE);
        }
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
     * sets the movie data for the activity
     *
     * @param movieData
     */
    private void setMovieData(Movie movieData) {
        detailsLoadingSpinner.setVisibility(View.GONE);
        mBinding.detailMovieContent.setVisibility(View.VISIBLE);
        mBinding.detailBackground.setVisibility(View.VISIBLE);

        //if data was loaded add it to the adapter
        if (movieData != null) {
            setMovieInformation(movieData);
        } else {
            showMovieNotLoaded();
        }
    }

    @Override
    public Loader<Movie> onCreateLoader(int i, Bundle bundle) {
        return new MovieDetailsAsyncLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Movie> loader, Movie movieData) {
        setMovieData(movieData);
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {

    }

    @Override
    public void onSelectTrailer(MoviePreview selectedMovie) {

    }
}
