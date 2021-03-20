package tuannt.simple.myapplication;

import android.app.Application;

import tuannt.simple.steganography.Stegy;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stegy.init(this);
    }
}
