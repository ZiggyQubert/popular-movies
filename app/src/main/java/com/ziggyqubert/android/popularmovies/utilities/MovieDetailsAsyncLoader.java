package com.ziggyqubert.android.popularmovies.utilities;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.ziggyqubert.android.popularmovies.MovieDetails;
import com.ziggyqubert.android.popularmovies.model.Movie;

import java.lang.ref.WeakReference;

public class MovieDetailsAsyncLoader extends AsyncTaskLoader<Movie> {

    //save the context as a weak reference
    private WeakReference<MovieDetails> activityReference;

    Movie cachedMovieData = null;

    public MovieDetailsAsyncLoader(Context context) {
        super(context);
        activityReference = new WeakReference<>((MovieDetails) context);
    }

    /**
     * helper function to get the current MovieList activity
     *
     * @return
     */
    private MovieDetails getMovieDetailsActivity() {
        MovieDetails movieDetailsActivity = activityReference.get();
        return movieDetailsActivity;
    }

    @Override
    protected void onStartLoading() {
        if (cachedMovieData == null) {
//            detailsLoadingSpinner.setVisibility(View.VISIBLE);
//            detailsMovieContnetView.setVisibility(View.INVISIBLE);
//            detailsBackgroundView.setVisibility(View.INVISIBLE);
            forceLoad();
        } else {
            deliverResult(cachedMovieData);
        }
    }

    @Override
    public Movie loadInBackground() {

        MovieDetails movieDetailsActivity = getMovieDetailsActivity();

        Movie movieData = null;
        if (movieDetailsActivity != null) {
            //fetching the movie details here as we need more information on the movie then we get in the list call
            movieData = ThemoviedbUtils.fetchMovieDetails(movieDetailsActivity.getPassedMovieId());
        }
        return movieData;
    }

    @Override
    public void deliverResult(Movie data) {
        cachedMovieData = data;
        super.deliverResult(data);
    }

}
