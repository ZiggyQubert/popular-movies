package com.ziggyqubert.android.popularmovies;

import android.app.Application;

import com.ziggyqubert.android.popularmovies.utilities.SSLCertificateChecking;

public class PopularMoviesApp extends Application {

    public static String APP_TAG = "popular-movies";

    @Override
    public void onCreate() {
        //added to resolve a network issue with my local corporate development environment
        SSLCertificateChecking.disableSSLCertificateChecking();

        super.onCreate();
    }

}
