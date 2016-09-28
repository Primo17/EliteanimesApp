package de.btcdev.eliteanimesapp;

import android.app.Activity;
import android.app.Application;
import android.os.AsyncTask;

import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Component;
import de.btcdev.eliteanimesapp.gui.LoginActivity;

@Singleton
@Component(modules = {AppModule.class})
public interface EaComponent {
    void inject(LoginActivity activity);
    void inject(Application app);
}
