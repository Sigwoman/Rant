package com.example.rant;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

public class MyApplication extends Application {
    public static Context context;
    public static final Handler mainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());


    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
