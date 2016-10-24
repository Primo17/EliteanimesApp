package de.btcdev.eliteanimesapp;

import android.app.Application;

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
