package com.ziggyqubert.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ziggyqubert.android.popularmovies.model.MoviePreview;

import java.util.ArrayList;
import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerAdapterViewHolder> {

    private List<MoviePreview> previewList;
    private final TrailerAdapterOnClickHandler mClickHandeler;

    public interface TrailerAdapterOnClickHandler {
        void onSelectTrailer(MoviePreview selectedMovie);
    }

    public TrailerAdapter(TrailerAdapter.TrailerAdapterOnClickHandler clickHandler) {
        mClickHandeler = clickHandler;
        previewList = new ArrayList<MoviePreview>();
    }

    @NonNull
    @Override
    public TrailerAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_details_trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new TrailerAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerAdapterViewHolder holder, int position) {
        MoviePreview currentPreview = previewList.get(position);
        holder.mTitleText.setText(currentPreview.getName());
    }

    @Override
    public int getItemCount() {
        return previewList.size();
    }

    public void setTrailerData(List<MoviePreview> newPreviewData) {
        previewList = newPreviewData;
        notifyDataSetChanged();
    }

    public class TrailerAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView mTitleText;

        public TrailerAdapterViewHolder(View view) {
            super(view);
            mTitleText = view.findViewById(R.id.trailer_title);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            MoviePreview selectedPreview = previewList.get(adapterPosition);
            mClickHandeler.onSelectTrailer(selectedPreview);
        }
    }
}
