package de.btcdev.eliteanimesapp;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;

import de.btcdev.eliteanimesapp.gui.LoginActivity;

public class EaApp extends Application {

    EaComponent eaComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        eaComponent = DaggerEaComponent.builder().appModule(new AppModule(this)).build();
    }

    public EaComponent getEaComponent() {
        return eaComponent;
    }

}
