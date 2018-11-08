package com.ziggyqubert.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.utilities.EndlessRecyclerOnScrollListener;
import com.ziggyqubert.android.popularmovies.utilities.ThemoviedbUtils;

import java.util.List;

public class MovieList extends AppCompatActivity
        implements MovieAdapter.MovieAdapterOnClickHandler {

    //number of columns to show in the grid view
    public static Integer LAYOUT_COLUMNS = 3;

    //counter for infinite scroll loading
    private Integer currentMoviesPage;
    private RecyclerView movieRecyclerView;
    private MovieAdapter movieAdapter;
    private GridLayoutManager layoutManager;
    private EndlesMovieScroll endlesMovieScroll;

    //loading indicator views
    private ProgressBar initialLoadingProgressBarView;
    private ProgressBar progressBarView;
    private TextView errorMessageView;

    //currently selected movie sort type, see constants in The
    private String movieSortType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        //sets up the loading values
        initialLoadingProgressBarView = findViewById(R.id.pb_page_loading_spinner);
        progressBarView = findViewById(R.id.pb_loading_spinner);
        errorMessageView = findViewById(R.id.tv_error_text);

        //sets up the recycler view
        movieRecyclerView = findViewById(R.id.rv_movie_list);
        layoutManager = new GridLayoutManager(this, LAYOUT_COLUMNS);
        movieRecyclerView.setLayoutManager(layoutManager);
        movieRecyclerView.setHasFixedSize(true);
        movieAdapter = new MovieAdapter(this);
        movieRecyclerView.setAdapter(movieAdapter);

        endlesMovieScroll = new EndlesMovieScroll();
        movieRecyclerView.addOnScrollListener(endlesMovieScroll);

        //sets up the content loading
        showMoviesBy(ThemoviedbUtils.SORT_MOST_POPULAR);
    }

    /**
     * clears the movie data and resets the scrolling / data load
     */
    private void resetMovieData() {
        movieAdapter.clearMovieData();
        layoutManager.scrollToPositionWithOffset(0, 0);
        currentMoviesPage = 1;
        endlesMovieScroll.reset();
        initialLoadingProgressBarView.setVisibility(View.VISIBLE);
    }

    /**
     * helper function to get a string resource by name
     *
     * @param aString
     * @return
     */
    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        return getString(resId);
    }

    /**
     * handles the sorting / inits the fetching of the movies by the sort type
     *
     * @param sortByValue
     */
    public void showMoviesBy(String sortByValue) {
        if (movieSortType != sortByValue) {
            Log.i(PopularMoviesApp.APP_TAG, "Setting sort to " + sortByValue);
            movieSortType = sortByValue;
            resetMovieData();
            new PopulateMovieQueryTask().execute(currentMoviesPage);
            setTitle(getStringResourceByName(sortByValue));
        }
    }

    /**
     * when a movie is selected call this
     *
     * @param selectedMovie
     */
    @Override
    public void onSelectMovie(Movie selectedMovie) {
        Log.i(PopularMoviesApp.APP_TAG, "Selected: " + selectedMovie.getTitle());

        Context context = this;
        Class destinationClass = MovieDetails.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_UID, selectedMovie.getId());

        startActivity(intentToStartDetailActivity);
    }

    /**
     * set up the endless scroll class for the movie list
     */
    public class EndlesMovieScroll extends EndlessRecyclerOnScrollListener {
        /**
         * loads the next page of data
         */
        @Override
        public void onLoadMore() {
            //incriment the page number
            currentMoviesPage++;
            Log.i(PopularMoviesApp.APP_TAG, "Load page " + currentMoviesPage);
            //start the loading task for the current page
            new PopulateMovieQueryTask().execute(currentMoviesPage);
        }
    }

    /**
     * shows the content areas
     */
    public void showContent() {
        movieRecyclerView.setVisibility(View.VISIBLE);
        errorMessageView.setVisibility(View.GONE);
    }

    /**
     * shows the error message
     */
    public void showError() {
        movieRecyclerView.setVisibility(View.GONE);
        errorMessageView.setVisibility(View.VISIBLE);
    }

    /**
     * Async task to load the popular movies data, takes the page of movie data to load, returns a list of movies
     */
    public class PopulateMovieQueryTask extends AsyncTask<Integer, Void, List<Movie>> {

        /**
         * shows the load spinner
         */
        @Override
        protected void onPreExecute() {
            progressBarView.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        /**
         * runs the query to fetch data
         *
         * @param pageNumbers
         * @return
         */
        @Override
        protected List<Movie> doInBackground(Integer... pageNumbers) {
            Integer pageNumberToLoad = pageNumbers[0];
            List<Movie> movieList;
            movieList = ThemoviedbUtils.fetchMovieList(movieSortType, pageNumberToLoad);
            return movieList;
        }

        /**
         * handles the response
         *
         * @param popularMovieList
         */
        @Override
        protected void onPostExecute(List<Movie> popularMovieList) {

            // hides the loading / progress indicators
            initialLoadingProgressBarView.setVisibility(View.GONE);
            progressBarView.setVisibility(View.GONE);

            //if data was loaded add it to the adapter
            if (popularMovieList != null && popularMovieList.size() > 0) {
                showContent();
                movieAdapter.addMovieData(popularMovieList);
            } else if (currentMoviesPage == 1) {
                //if the first page then show an error
                showError();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sort_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String newSortBy;
        int id = item.getItemId();

        switch (id) {
            //the sort by most popular selection

            //the sort by top rated selection
            case R.id.action_sort_top_rated:
                newSortBy = ThemoviedbUtils.SORT_TOP_RATED;
                break;

            //the sort by top rated selection
            case R.id.action_sort_now_playing:
                newSortBy = ThemoviedbUtils.SORT_NOW_PLAYING;
                break;

            //the sort by top rated selection
            case R.id.action_sort_upcoming:
                newSortBy = ThemoviedbUtils.SORT_UPCOMING;
                break;

            case R.id.action_sort_most_popular:
            default:
                newSortBy = ThemoviedbUtils.SORT_MOST_POPULAR;
                break;
        }
        item.setChecked(true);
        showMoviesBy(newSortBy);
        return true;
    }
}