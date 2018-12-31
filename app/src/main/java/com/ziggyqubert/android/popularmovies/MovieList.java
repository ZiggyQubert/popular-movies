package com.ziggyqubert.android.popularmovies;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
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

import com.ziggyqubert.android.popularmovies.database.AppDatabase;
import com.ziggyqubert.android.popularmovies.database.FavoritesEntry;
import com.ziggyqubert.android.popularmovies.model.Movie;
import com.ziggyqubert.android.popularmovies.utilities.EndlessRecyclerOnScrollListener;
import com.ziggyqubert.android.popularmovies.utilities.ThemoviedbUtils;

import java.util.ArrayList;
import java.util.List;

public class MovieList extends AppCompatActivity
        implements MovieAdapter.MovieAdapterOnClickHandler {

    public static final Integer LAYOUT_COLUMNS_PORTRAIT = 3;
    public static final Integer LAYOUT_COLUMNS_LANDSCAPE = 5;

    //number of columns to show in the grid view
    public static Integer LAYOUT_COLUMNS;

    private RecyclerView movieRecyclerView;
    private MovieAdapter movieAdapter;
    private MovieListViewModel movieListViewModel;
    private GridLayoutManager layoutManager;
    private EndlesMovieScroll endlesMovieScroll;

    //loading indicator views
    private ProgressBar initialLoadingProgressBarView;
    private ProgressBar progressBarView;
    private TextView errorMessageView;
    private TextView noResultsView;

    private SwipeRefreshLayout swipeRefreshLayout;

    private AppDatabase mDb;
    private LiveData<List<FavoritesEntry>> favoritesEntrys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_list_activity);

        mDb = AppDatabase.getInstance(getApplication().getApplicationContext());

        //sets the number of columns based on the orientation
        Integer currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            LAYOUT_COLUMNS = LAYOUT_COLUMNS_LANDSCAPE;
        } else {
            LAYOUT_COLUMNS = LAYOUT_COLUMNS_PORTRAIT;
        }

        favoritesEntrys = null;
        //sets up the loading values
        initialLoadingProgressBarView = findViewById(R.id.pb_page_loading_spinner);
        progressBarView = findViewById(R.id.pb_loading_spinner);
        errorMessageView = findViewById(R.id.tv_error_text);
        noResultsView = findViewById(R.id.tv_no_results_text);

        //sets up the recycler view
        movieRecyclerView = findViewById(R.id.rv_movie_list);
        layoutManager = new GridLayoutManager(this, LAYOUT_COLUMNS);
        movieRecyclerView.setLayoutManager(layoutManager);
        movieRecyclerView.setHasFixedSize(true);
        movieAdapter = new MovieAdapter(this);
        movieRecyclerView.setAdapter(movieAdapter);

        //sets up the endless scrolling
        endlesMovieScroll = new EndlesMovieScroll();
        movieRecyclerView.addOnScrollListener(endlesMovieScroll);

        //sets up the refresh swipe
        swipeRefreshLayout = findViewById(R.id.movieList_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.i(PopularMoviesApp.APP_TAG, "onRefresh called from SwipeRefreshLayout");
                        resetMovieData();
                        showLoading();
                        showMoviesBy(movieListViewModel.getSortType(), false);
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        movieListViewModel = getViewModel();
        Log.i(PopularMoviesApp.APP_TAG, "Load movie data from view model");
        setTitle(getStringResourceByName(movieListViewModel.getSortType()));
        movieAdapter.setMovieData(movieListViewModel.getMovieList());
        if (movieListViewModel.getCurrentPage() < 1) {
            showMoviesBy(movieListViewModel.getSortType());
        } else {
            if (movieListViewModel.getSortType() == ThemoviedbUtils.SORT_FAVORITES) {
                setUpFavoritesObserver();
            }
            showSuccess();
            showContent();
        }
    }

    private MovieListViewModel getViewModel() {
        MovieListViewModel viewModel = ViewModelProviders.of(this).get(MovieListViewModel.class);
        return viewModel;
    }


    /**
     * clears the movie data and resets the scrolling / data load
     */
    private void resetMovieData() {
        movieListViewModel.resetCurrentPage();
        endlesMovieScroll.reset();
        movieAdapter.clearMovieData();

        movieRecyclerView.getRecycledViewPool().clear();

        layoutManager.scrollToPositionWithOffset(0, 0);
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
        if (movieListViewModel.getSortType() != sortByValue || movieAdapter.getItemCount() < 1) {
            Log.i(PopularMoviesApp.APP_TAG, "Setting sort to " + sortByValue);
            showLoading();
            movieListViewModel.setSortType(sortByValue);
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

    public void setUpFavoritesObserver() {
        if (favoritesEntrys == null) {
            favoritesEntrys = mDb.taskDao().loadAllFavorites();
            favoritesEntrys.observe(this, new Observer<List<FavoritesEntry>>() {
                @Override
                public void onChanged(@Nullable List<FavoritesEntry> favoritesEntries) {
                    List<Movie> movieList = new ArrayList<Movie>();
                    for (int fvIdx = 0; fvIdx < favoritesEntries.size(); fvIdx++) {
                        movieList.add(new Movie(favoritesEntries.get(fvIdx)));
                    }
                    movieAdapter.setMovieData(movieList);
                    showSuccess();
                    showContent();
                }
            });
        }
    }

    public void removeFavoritesObserver() {
        if (favoritesEntrys != null) {
            favoritesEntrys.removeObservers(this);
            favoritesEntrys = null;
        }
    }

    /**
     * Loads the next page of movies
     */
    public void loadNextPageOfMovies() {
        movieListViewModel.incrimentCurrentPage();
        Log.i(PopularMoviesApp.APP_TAG, "loadNextPageOfMovies " + movieListViewModel.getSortType() + " pg: " + movieListViewModel.getCurrentPage());
        if (movieListViewModel.getSortType().equals(ThemoviedbUtils.SORT_FAVORITES)) {
            if (movieListViewModel.getCurrentPage() == 1) {
                setUpFavoritesObserver();
            }
        } else {
            removeFavoritesObserver();
            //incriments the movie page count
            movieListViewModel.incrimentCurrentPage();
            progressBarView.setVisibility(View.VISIBLE);
            //performs the loading action
            new PopulateMovieQueryTask().execute(movieListViewModel.getCurrentPage());
        }
    }

    public void addDataToMovieAdapter(final List<Movie> newMovieData) {
        // hides the loading / progress indicators
        showSuccess();

        //if data was loaded add it to the adapter
        if (newMovieData != null && newMovieData.size() > 0) {
            Log.i(PopularMoviesApp.APP_TAG, "addDataToMovieAdapter ADDING CONTENT");

            movieRecyclerView.post(new Runnable() {
                public void run() {
                    movieAdapter.addMovieData(newMovieData);
                    showContent();
                }
            });
        } else if (movieListViewModel.getCurrentPage() < 2) {
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
            Log.i(PopularMoviesApp.APP_TAG, "onLoadMore " + movieListViewModel.getSortType() + " pg: " + movieListViewModel.getCurrentPage());
            //start the loading task for the current page
            loadNextPageOfMovies();
        }
    }

    public void showLoading() {
        noResultsView.setVisibility(View.GONE);
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
        if (movieAdapter.getItemCount() > 0) {
            movieRecyclerView.setVisibility(View.VISIBLE);
            noResultsView.setVisibility(View.GONE);
        } else {
            noResultsView.setVisibility(View.VISIBLE);
            movieRecyclerView.setVisibility(View.GONE);
        }
        errorMessageView.setVisibility(View.GONE);
    }

    /**
     * shows the error message
     */
    public void showError() {
        noResultsView.setVisibility(View.GONE);
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
            Log.i(PopularMoviesApp.APP_TAG, "doInBackground " + movieListViewModel.getSortType() + " pg: " + movieListViewModel.getCurrentPage());
            Integer pageNumberToLoad = pageNumbers[0];
            List<Movie> movieList;
            movieList = ThemoviedbUtils.fetchMovieList(movieListViewModel.getSortType(), pageNumberToLoad);
            return movieList;
        }

        /**
         * handles the response
         *
         * @param movieList
         */
        @Override
        protected void onPostExecute(List<Movie> movieList) {
            Log.i(PopularMoviesApp.APP_TAG, "onPostExecute " + movieListViewModel.getSortType() + " pg: " + movieListViewModel.getCurrentPage());
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
        String menuItemIdString = "action_sort_" + movieListViewModel.getSortType();
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

            //the sort by top rated selection
            case R.id.action_sort_favorites:
                newSortBy = ThemoviedbUtils.SORT_FAVORITES;
                break;

            //sort by popular (the default)
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