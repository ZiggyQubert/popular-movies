package com.ziggyqubert.android.popularmovies.model;

import com.ziggyqubert.android.popularmovies.utilities.DataParsers;

import org.json.JSONObject;

public class MovieReview {
    private String id;
    private String author;
    private String content;
    private String url;

    public MovieReview(JSONObject jsonData) {
        id = DataParsers.safeGetStringFromJson(jsonData, "id");
        author = DataParsers.safeGetStringFromJson(jsonData, "author");
        content = DataParsers.safeGetStringFromJson(jsonData, "content");
        url = DataParsers.safeGetStringFromJson(jsonData, "url");
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
