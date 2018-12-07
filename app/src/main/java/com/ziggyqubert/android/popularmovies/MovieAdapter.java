package com.ziggyqubert.android.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.ziggyqubert.android.popularmovies.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private List<Movie> movieList;
    private final MovieAdapterOnClickHandler mClickHandeler;

    public interface MovieAdapterOnClickHandler {
        void onSelectMovie(Movie selectedMovie);
    }

    /**
     * initialize the movie list
     */
    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        //inits the displayed data  list
        movieList = new ArrayList<Movie>();
        mClickHandeler = clickHandler;
    }

    /**
     * gets the current list of all movies that have ben loaded
     *
     * @return
     */
    public List<Movie> getAllItems() {
        return movieList;
    }

    @NonNull
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        //sets the height of the poster to be 1.5 x the displayed width of the poster
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        Double height = viewGroup.getMeasuredWidth() / MovieList.LAYOUT_COLUMNS * 1.5;
        layoutParams.height = (int) Math.round(height);

        return new MovieAdapterViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MovieAdapterViewHolder movieAdapterViewHolder, int position) {

        Movie movieData = movieList.get(position);
        String movieTitle = movieData.getTitle();

        movieAdapterViewHolder.mTitleText.setVisibility(View.GONE);
        movieAdapterViewHolder.mTitleText.setText(movieTitle);

        //loads the movie poster
        Picasso.get()
                .load(movieData.getPosterUrl().toString())
                //error image taken from http://icons-for-free.com/
                .error(R.drawable.movie_not_found)
                .into(movieAdapterViewHolder.mPoster, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(PopularMoviesApp.APP_TAG, "Error loading image");
                        movieAdapterViewHolder.mTitleText.setVisibility(View.VISIBLE);
                        e.printStackTrace();
                    }
                });
    }

    /**
     * gets the number of items currently loaded for the view
     *
     * @return
     */
    @Override
    public int getItemCount() {
        if (movieList != null) {
            return movieList.size();
        } else {
            return 0;
        }
    }

    /**
     * removes all movie data
     */
    public void clearMovieData() {
        movieList.clear();
        notifyDataSetChanged();
    }

    /**
     * Sets the movie data to the specified data
     *
     * @param movieListData
     */
    public void setMovieData(List<Movie> movieListData) {
        clearMovieData();
        addMovieData(movieListData);
    }

    /**
     * adds to the list of movies
     *
     * @param movieListData
     */
    public void addMovieData(List<Movie> movieListData) {
        movieList.addAll(movieListData);
        notifyDataSetChanged();
    }

    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTitleText;
        public final ImageView mPoster;

        public MovieAdapterViewHolder(View view) {
            super(view);
            mTitleText = view.findViewById(R.id.m_movie_title);
            mPoster = view.findViewById(R.id.m_movie_poster);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            Movie selectedMovie = movieList.get(adapterPosition);
            mClickHandeler.onSelectMovie(selectedMovie);
        }
    }

}
