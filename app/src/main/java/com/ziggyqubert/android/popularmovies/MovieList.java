package com.ziggyqubert.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.io.Serializable;
import java.util.List;

public class MovieList extends AppCompatActivity
        implements MovieAdapter.MovieAdapterOnClickHandler {

    public static final Integer LAYOUT_COLUMNS_PORTRAIT = 3;
    public static final Integer LAYOUT_COLUMNS_LANDSCAPE = 5;

    public static final String SAVED_MOVIE_SORT_TYPE_KEY = "saveMovieSortType";
    public static final String SAVED_CURRENT_MOVIES_PAGE_KEY = "saveCurrentMoviesPage";
    public static final String SAVED_MOVIE_LIST_DATA_KEY = "saveMovieListData";


    //number of columns to show in the grid view
    public static Integer LAYOUT_COLUMNS;

    //counter for infinite scroll loading
    private Integer currentMoviesPage;
    //currently selected movie sort type, see constants in The
    private String movieSortType;

    private RecyclerView movieRecyclerView;
    private MovieAdapter movieAdapter;
    private GridLayoutManager layoutManager;
    private EndlesMovieScroll endlesMovieScroll;

    //loading indicator views
    private ProgressBar initialLoadingProgressBarView;
    private ProgressBar progressBarView;
    private TextView errorMessageView;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Integer currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            LAYOUT_COLUMNS = LAYOUT_COLUMNS_LANDSCAPE;
        } else {
            LAYOUT_COLUMNS = LAYOUT_COLUMNS_PORTRAIT;
        }

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

        swipeRefreshLayout = findViewById(R.id.movieList_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(PopularMoviesApp.APP_TAG, "onRefresh called from SwipeRefreshLayout");
                        resetMovieData();
                        showLoading();
                        showMoviesBy(movieSortType, false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        //if loading from a saved instance load the data from the saved instance state
        if (savedInstanceState != null) {

            Log.i(PopularMoviesApp.APP_TAG, "Load activity from saved instance state");

            resetMovieData();

            if (savedInstanceState.containsKey(SAVED_MOVIE_SORT_TYPE_KEY)) {
                movieSortType = savedInstanceState.getString(SAVED_MOVIE_SORT_TYPE_KEY);
            }
            setTitle(getStringResourceByName(movieSortType));

            if (savedInstanceState.containsKey(SAVED_CURRENT_MOVIES_PAGE_KEY)) {
                currentMoviesPage = savedInstanceState.getInt(SAVED_CURRENT_MOVIES_PAGE_KEY);
            }

            if (savedInstanceState.containsKey(SAVED_MOVIE_LIST_DATA_KEY)) {
                final List<Movie> savedMovieList = (List<Movie>) savedInstanceState.getSerializable(SAVED_MOVIE_LIST_DATA_KEY);
                addDataToMovieAdapter(savedMovieList);
//                initialLoad = false;
            } else {
                showMoviesBy(movieSortType);
            }
        } else {
            Log.i(PopularMoviesApp.APP_TAG, "Load activity with defaults");
            showMoviesBy(ThemoviedbUtils.SORT_MOST_POPULAR);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.i(PopularMoviesApp.APP_TAG, "onSaveInstanceState");

        outState.putString(SAVED_MOVIE_SORT_TYPE_KEY, movieSortType);
        outState.putInt(SAVED_CURRENT_MOVIES_PAGE_KEY, currentMoviesPage);
        outState.putSerializable(SAVED_MOVIE_LIST_DATA_KEY, (Serializable) movieAdapter.getAllItems());
    }

    /**
     * clears the movie data and resets the scrolling / data load
     */
    private void resetMovieData() {
        layoutManager.scrollToPositionWithOffset(0, 0);
        currentMoviesPage = 0;
        endlesMovieScroll.reset();
        movieAdapter.clearMovieData();
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
        showMoviesBy(sortByValue, true);
    }

    public void showMoviesBy(String sortByValue, boolean loadNextPage) {
        if (movieSortType != sortByValue || movieAdapter.getItemCount() < 1) {
            Log.i(PopularMoviesApp.APP_TAG, "Setting sort to " + sortByValue);
            showLoading();
            movieSortType = sortByValue;
            resetMovieData();
            loadNextPageOfMovies();
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
     * Loads the next page of movies
     */
    public void loadNextPageOfMovies() {
        //incriments the movie page count
        currentMoviesPage++;
        Log.i(PopularMoviesApp.APP_TAG, "loadNextPageOfMovies " + movieSortType + " pg: " + currentMoviesPage);
        progressBarView.setVisibility(View.VISIBLE);
        //performs the loading action
        new PopulateMovieQueryTask().execute(currentMoviesPage);
    }

    public void addDataToMovieAdapter(final List<Movie> newMovieData) {
        // hides the loading / progress indicators
        showSuccess();

        //if data was loaded add it to the adapter
        if (newMovieData != null && newMovieData.size() > 0) {
            Log.i(PopularMoviesApp.APP_TAG, "addDataToMovieAdapter ADDING CONTENT");

            showContent();
            movieRecyclerView.post(new Runnable() {
                public void run() {
                    movieAdapter.addMovieData(newMovieData);
                }
            });
        } else if (currentMoviesPage < 2) {
            //if the first page then show an error
            showError();
        }
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
            Log.i(PopularMoviesApp.APP_TAG, "onLoadMore " + movieSortType + " pg: " + currentMoviesPage);
            //start the loading task for the current page
            loadNextPageOfMovies();
        }
    }

    public void showLoading() {
        movieRecyclerView.setVisibility(View.GONE);
        errorMessageView.setVisibility(View.GONE);
        initialLoadingProgressBarView.setVisibility(View.VISIBLE);
    }

    /**
     * shows the success state
     */
    public void showSuccess() {
        initialLoadingProgressBarView.setVisibility(View.GONE);
        progressBarView.setVisibility(View.GONE);
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
     * using this instead of the asyncloader as issues with multiple requests happening at once
     */
    public class PopulateMovieQueryTask extends AsyncTask<Integer, Void, List<Movie>> {

        /**
         * runs the query to fetch data
         *
         * @param pageNumbers
         * @return
         */
        @Override
        protected List<Movie> doInBackground(Integer... pageNumbers) {
            Log.i(PopularMoviesApp.APP_TAG, "doInBackground " + movieSortType + " pg: " + currentMoviesPage);
            Integer pageNumberToLoad = pageNumbers[0];
            List<Movie> movieList;
            movieList = ThemoviedbUtils.fetchMovieList(movieSortType, pageNumberToLoad);
            return movieList;
        }

        /**
         * handles the response
         *
         * @param movieList
         */
        @Override
        protected void onPostExecute(List<Movie> movieList) {
            Log.i(PopularMoviesApp.APP_TAG, "onPostExecute " + movieSortType + " pg: " + currentMoviesPage);
            addDataToMovieAdapter(movieList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.sort_menu, menu);
        /* Return true so that the menu is displayed in the Toolbar */

        //sets teh selected menu item
        String menuItemIdString = "action_sort_" + movieSortType;
        int menuItemResourceId = this.getResources().getIdentifier(menuItemIdString, "id", getPackageName());
        menu.findItem(menuItemResourceId).setChecked(true);

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

            case R.id.action_sort_popular:
            default:
                newSortBy = ThemoviedbUtils.SORT_MOST_POPULAR;
                break;
        }
        item.setChecked(true);
        showMoviesBy(newSortBy, false);
        return true;
    }
}