package de.btcdev.eliteanimesapp;

import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.btcdev.eliteanimesapp.data.Board;
import de.btcdev.eliteanimesapp.data.BoardPost;
import de.btcdev.eliteanimesapp.data.BoardThread;
import de.btcdev.eliteanimesapp.data.Comment;
import de.btcdev.eliteanimesapp.data.Friend;
import de.btcdev.eliteanimesapp.data.FriendRequest;
import de.btcdev.eliteanimesapp.data.ListAnime;
import de.btcdev.eliteanimesapp.data.NetworkService;
import de.btcdev.eliteanimesapp.data.PrivateMessage;
import de.btcdev.eliteanimesapp.data.Profile;
import de.btcdev.eliteanimesapp.data.Statistics;
import de.btcdev.eliteanimesapp.data.User;
import de.btcdev.eliteanimesapp.json.BoardDeserializer;
import de.btcdev.eliteanimesapp.json.BoardPostDeserializer;
import de.btcdev.eliteanimesapp.json.BoardThreadDeserializer;
import de.btcdev.eliteanimesapp.json.CommentDeserializer;
import de.btcdev.eliteanimesapp.json.CommentSerializer;
import de.btcdev.eliteanimesapp.json.FriendDeserializer;
import de.btcdev.eliteanimesapp.json.FriendRequestDeserializer;
import de.btcdev.eliteanimesapp.json.ListAnimeDeserializer;
import de.btcdev.eliteanimesapp.json.ListAnimeSerializer;
import de.btcdev.eliteanimesapp.json.PrivateMessageDeserializer;
import de.btcdev.eliteanimesapp.json.PrivateMessageSerializer;
import de.btcdev.eliteanimesapp.json.ProfileDeserializer;
import de.btcdev.eliteanimesapp.json.SearchUserDeserializer;
import de.btcdev.eliteanimesapp.json.StatisticsDeserializer;
import de.btcdev.eliteanimesapp.services.LoginService;

@Module
public class AppModule {

    Application eaApp = null;

    public AppModule(Application application) {
        eaApp = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return eaApp;
    }

    @Provides
    @Singleton
    NetworkService provideNetworkService(Application application) {
        return new NetworkService(application.getApplicationContext());
    }

    @Provides
    @Singleton
    LoginService provideLoginService(NetworkService networkService) {
        return new LoginService(networkService);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Board.class, new BoardDeserializer());
        gsonBuilder.registerTypeAdapter(BoardPost.class, new BoardPostDeserializer());
        gsonBuilder.registerTypeAdapter(BoardThread.class, new BoardThreadDeserializer());
        gsonBuilder.registerTypeAdapter(Comment.class, new CommentDeserializer());
        gsonBuilder.registerTypeAdapter(Comment.class, new CommentSerializer());
        gsonBuilder.registerTypeAdapter(Friend.class, new FriendDeserializer());
        gsonBuilder.registerTypeAdapter(FriendRequest.class, new FriendRequestDeserializer());
        gsonBuilder.registerTypeAdapter(ListAnime.class, new ListAnimeDeserializer());
        gsonBuilder.registerTypeAdapter(ListAnime.class, new ListAnimeSerializer());
        gsonBuilder.registerTypeAdapter(PrivateMessage.class, new PrivateMessageDeserializer());
        gsonBuilder.registerTypeAdapter(PrivateMessage.class, new PrivateMessageSerializer());
        gsonBuilder.registerTypeAdapter(Profile.class, new ProfileDeserializer());
        gsonBuilder.registerTypeAdapter(User.class, new SearchUserDeserializer());
        gsonBuilder.registerTypeAdapter(Statistics.class, new StatisticsDeserializer());
        return gsonBuilder.create();
    }
}
