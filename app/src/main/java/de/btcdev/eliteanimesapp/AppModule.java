package de.btcdev.eliteanimesapp;

import android.content.Context;

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
import de.btcdev.eliteanimesapp.services.AnimeService;
import de.btcdev.eliteanimesapp.services.BoardService;
import de.btcdev.eliteanimesapp.services.CommentService;
import de.btcdev.eliteanimesapp.services.ConfigurationService;
import de.btcdev.eliteanimesapp.services.FriendService;
import de.btcdev.eliteanimesapp.services.ImageService;
import de.btcdev.eliteanimesapp.services.LoginService;
import de.btcdev.eliteanimesapp.services.NotificationService;
import de.btcdev.eliteanimesapp.services.PrivateMessageService;
import de.btcdev.eliteanimesapp.services.ProfileService;
import de.btcdev.eliteanimesapp.services.UserService;

@Module
public class AppModule {

    EaApp eaApp = null;

    public AppModule(EaApp application) {
        eaApp = application;
    }

    @Provides
    @Singleton
    EaApp providesApplication() {
        return eaApp;
    }

    @Provides
    Context provideContext() {
        return eaApp.getApplicationContext();
    }

    @Provides
    @Singleton
    ConfigurationService provideConfigurationService(EaApp eaApp) {
        return new ConfigurationService(eaApp);
    }

    @Provides
    @Singleton
    NetworkService provideNetworkService(EaApp application, ConfigurationService configurationService) {
        return new NetworkService(application.getApplicationContext(), configurationService);
    }

    @Provides
    LoginService provideLoginService(NetworkService networkService, ConfigurationService configurationService) {
        return new LoginService(networkService, configurationService);
    }

    @Provides
    ProfileService provideProfileService(NetworkService networkService, ImageService imageService, ConfigurationService configurationService) {
        return new ProfileService(networkService, imageService, configurationService);
    }

    @Provides
    CommentService provideCommentService(NetworkService networkService, ImageService imageService, ConfigurationService configurationService) {
        return new CommentService(networkService, imageService, configurationService);
    }

    @Provides
    PrivateMessageService providePrivateMessageService(NetworkService networkService) {
        return new PrivateMessageService(networkService);
    }

    @Provides
    FriendService provideFriendService(NetworkService networkService, ConfigurationService configurationService) {
        return new FriendService(networkService, configurationService);
    }

    @Provides
    UserService provideUserService(NetworkService networkService, ConfigurationService configurationService) {
        return new UserService(networkService, configurationService);
    }

    @Provides
    AnimeService provideAnimeService(NetworkService networkService, ConfigurationService configurationService) {
        return new AnimeService(networkService, configurationService);
    }

    @Provides
    BoardService provideBoardService(NetworkService networkService, ImageService imageService, ConfigurationService configurationService){
        return new BoardService(networkService, imageService, configurationService);
    }

    @Provides
    ImageService provideImageService(NetworkService networkService, Context context) {
        return new ImageService(networkService, context);
    }

    @Provides
    NotificationService provideNotificationService(NetworkService networkService, ConfigurationService configurationService) {
        return new NotificationService(networkService, configurationService);
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
