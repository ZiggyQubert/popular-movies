package com.ziggyqubert.android.popularmovies;

import android.app.LoaderManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.Loader;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.ziggyqubert.android.popularmovies.database.FavoritesEntry;
import com.ziggyqubert.android.popularmovies.databinding.MovieDetailsActivityBinding;
import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.model.MoviePreview;
import com.ziggyqubert.android.popularmovies.model.MovieReview;
import com.ziggyqubert.android.popularmovies.utilities.MovieDetailsAsyncLoader;
import com.ziggyqubert.android.popularmovies.utilities.ThemoviedbUtils;

import java.util.List;

public class MovieDetails extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Movie>, TrailerAdapter.TrailerAdapterOnClickHandler {

    Integer MOVIE_DETAILS_LOADER_ID = 1;

    Movie selectedMovieData;

    ProgressBar detailsLoadingSpinner;
    View detailsMovieContnetView;

    MovieDetailsViewModel movieDetailsViewModel;

    MovieDetailsActivityBinding mBinding;

    private RecyclerView trailerRecyclerView;
    private TrailerAdapter trailerAdapter;

    private RecyclerView reviewRecyclerView;
    private ReviewAdapter reviewAdapter;

    private Menu favoritesMenu;

    /**
     * gets the movie id that was passed to the details screen
     *
     * @return
     */
    public Integer getPassedMovieId() {
        return movieDetailsViewModel.getPassedMovieId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_details_activity);

        movieDetailsViewModel = getViewModel();
        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_details_activity);

        detailsLoadingSpinner = findViewById(R.id.detail_loading_spinner);
        detailsMovieContnetView = findViewById(R.id.detail_movie_content);


        //sets up the recycler view for trailers
        trailerRecyclerView = findViewById(R.id.trailer_display_recyclerView);
        trailerRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(this);
        trailerRecyclerView.setLayoutManager(trailerLayoutManager);
        trailerAdapter = new TrailerAdapter(this);
        trailerRecyclerView.setAdapter(trailerAdapter);

        //sets up the recycler view for trailers
        reviewRecyclerView = findViewById(R.id.review_display_recyclerView);
        reviewRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager reviewLayoutManager = new LinearLayoutManager(this);
        reviewRecyclerView.setLayoutManager(reviewLayoutManager);
        reviewAdapter = new ReviewAdapter();
        reviewRecyclerView.setAdapter(reviewAdapter);

        Intent intentThatStartedThisActivity = getIntent();

        if (movieDetailsViewModel.getPassedMovieId() == null && intentThatStartedThisActivity.hasExtra(Intent.EXTRA_UID)) {
            Integer passedMovieId = intentThatStartedThisActivity.getIntExtra(Intent.EXTRA_UID, 0);
            if (passedMovieId > 0) {
                movieDetailsViewModel.setPassedMovieId(passedMovieId);
            }
        }

        if (movieDetailsViewModel.getPassedMovieId() == null) {
            showMovieNotLoaded();
        } else {
            LoaderManager.LoaderCallbacks<Movie> callback = MovieDetails.this;
            Bundle bundleForLoader = null;
            detailsLoadingSpinner.setVisibility(View.VISIBLE);
            detailsMovieContnetView.setVisibility(View.INVISIBLE);
            mBinding.detailBackground.setVisibility(View.INVISIBLE);
            getLoaderManager().initLoader(MOVIE_DETAILS_LOADER_ID, bundleForLoader, callback);

        }
    }

    private MovieDetailsViewModel getViewModel() {
        MovieDetailsViewModel viewModel = ViewModelProviders.of(this).get(MovieDetailsViewModel.class);
        return viewModel;
    }

    public void setFavoritesMenuButtons(Boolean isFavorite) {
        if (isFavorite) {
            favoritesMenu.findItem(R.id.action_add_favorite).setVisible(false);
            favoritesMenu.findItem(R.id.action_remove_favorite).setVisible(true);
        } else {
            favoritesMenu.findItem(R.id.action_remove_favorite).setVisible(false);
            favoritesMenu.findItem(R.id.action_add_favorite).setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.favorites_menu, menu);

        favoritesMenu = menu;

        movieDetailsViewModel.getFavoriteData().observe(this, new Observer<FavoritesEntry>() {
            @Override
            public void onChanged(@Nullable FavoritesEntry favoritesEntry) {
                setFavoritesMenuButtons(favoritesEntry != null);
            }
        });

        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //set the favorite status of the current movie
        switch (id) {
            case R.id.action_add_favorite:
                movieDetailsViewModel.setFavorite(true, selectedMovieData);
                break;

            case R.id.action_remove_favorite:
                movieDetailsViewModel.setFavorite(false, selectedMovieData);
                break;
        }
        return true;
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

        List<MovieReview> reviewData = movieData.getReviews();
        View reviewArea = findViewById(R.id.detail_reviews);
        if (reviewData.size() > 0) {
            reviewArea.setVisibility(View.VISIBLE);
            reviewAdapter.setReviewData(movieData.getReviews());
        } else {
            reviewArea.setVisibility(View.GONE);
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
    public void onSelectTrailer(MoviePreview selectedtrailer) {
        Log.i(PopularMoviesApp.APP_TAG, "Selected trailer: " + selectedtrailer.getName());
        ThemoviedbUtils.openYoutubeVideo(this, selectedtrailer.getKey());
    }


}
