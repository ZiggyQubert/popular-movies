package com.ziggyqubert.android.popularmovies.database;

import android.arch.persistence.room.TypeConverter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class DataConverters {

    // date converters
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    //url converters
    @TypeConverter
    public static URL toURL(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    @TypeConverter
    public static String toURLString(URL url) {
        return url.toString();
    }
}
