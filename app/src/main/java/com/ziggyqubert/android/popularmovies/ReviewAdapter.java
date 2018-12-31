package com.ziggyqubert.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ziggyqubert.android.popularmovies.model.MovieReview;

import java.util.ArrayList;
import java.util.List;


public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    List<MovieReview> reviewList;

    public ReviewAdapter() {
        reviewList = new ArrayList<MovieReview>();
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_details_review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        MovieReview currentReview = reviewList.get(position);
        holder.mAuthorText.setText(currentReview.getAuthor());
        holder.mContentText.setText(currentReview.getContent());
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void setReviewData(List<MovieReview> newReviewData) {
        reviewList = newReviewData;
        Log.i("ADAPTER","SET DATA: " + reviewList.size());
        notifyDataSetChanged();
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder {

        public final TextView mAuthorText;
        public final TextView mContentText;

        public ReviewAdapterViewHolder(View view) {
            super(view);
            mAuthorText = view.findViewById(R.id.review_author);
            mContentText = view.findViewById(R.id.review_content);
        }

    }
}
