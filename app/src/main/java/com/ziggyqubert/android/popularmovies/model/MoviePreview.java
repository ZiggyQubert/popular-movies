package com.ziggyqubert.android.popularmovies.model;

import com.ziggyqubert.android.popularmovies.utilities.DataParsers;

import org.json.JSONObject;

public class MoviePreview {
    private String id;
    private String key;
    private String name;
    private String site;
    private Integer size;
    private String type;

    public MoviePreview(JSONObject jsonData) {
        id = DataParsers.safeGetStringFromJson(jsonData, "id");
        key = DataParsers.safeGetStringFromJson(jsonData, "key");
        name = DataParsers.safeGetStringFromJson(jsonData, "name");
        site = DataParsers.safeGetStringFromJson(jsonData, "site");
        size = DataParsers.safeGetIntFromJson(jsonData, "size");
        type = DataParsers.safeGetStringFromJson(jsonData, "type");
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getSite() {
        return site;
    }
}
