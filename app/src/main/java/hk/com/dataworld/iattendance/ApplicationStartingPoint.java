package hk.com.dataworld.iattendance;

import android.app.Application;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class ApplicationStartingPoint extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceProvider.registerDefaultIconSets();
    }
}
