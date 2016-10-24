package de.btcdev.eliteanimesapp;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.btcdev.eliteanimesapp.data.json.BoardDeserializer;
import de.btcdev.eliteanimesapp.data.json.BoardPostDeserializer;
import de.btcdev.eliteanimesapp.data.json.BoardThreadDeserializer;
import de.btcdev.eliteanimesapp.data.json.CommentDeserializer;
import de.btcdev.eliteanimesapp.data.json.CommentSerializer;
import de.btcdev.eliteanimesapp.data.json.FriendDeserializer;
import de.btcdev.eliteanimesapp.data.json.FriendRequestDeserializer;
import de.btcdev.eliteanimesapp.data.json.ListAnimeDeserializer;
import de.btcdev.eliteanimesapp.data.json.ListAnimeSerializer;
import de.btcdev.eliteanimesapp.data.json.PrivateMessageDeserializer;
import de.btcdev.eliteanimesapp.data.json.PrivateMessageSerializer;
import de.btcdev.eliteanimesapp.data.json.ProfileDeserializer;
import de.btcdev.eliteanimesapp.data.json.SearchUserDeserializer;
import de.btcdev.eliteanimesapp.data.json.StatisticsDeserializer;
import de.btcdev.eliteanimesapp.data.models.Board;
import de.btcdev.eliteanimesapp.data.models.BoardPost;
import de.btcdev.eliteanimesapp.data.models.BoardThread;
import de.btcdev.eliteanimesapp.data.models.Comment;
import de.btcdev.eliteanimesapp.data.models.Friend;
import de.btcdev.eliteanimesapp.data.models.FriendRequest;
import de.btcdev.eliteanimesapp.data.models.ListAnime;
import de.btcdev.eliteanimesapp.data.models.PrivateMessage;
import de.btcdev.eliteanimesapp.data.models.Profile;
import de.btcdev.eliteanimesapp.data.models.Statistics;
import de.btcdev.eliteanimesapp.data.models.User;
import de.btcdev.eliteanimesapp.data.services.AnimeService;
import de.btcdev.eliteanimesapp.data.services.BoardService;
import de.btcdev.eliteanimesapp.data.services.CommentService;
import de.btcdev.eliteanimesapp.data.services.ConfigurationService;
import de.btcdev.eliteanimesapp.data.services.FriendService;
import de.btcdev.eliteanimesapp.data.services.ImageService;
import de.btcdev.eliteanimesapp.data.services.LoginService;
import de.btcdev.eliteanimesapp.data.services.NetworkService;
import de.btcdev.eliteanimesapp.data.services.NotificationService;
import de.btcdev.eliteanimesapp.data.services.PrivateMessageService;
import de.btcdev.eliteanimesapp.data.services.ProfileService;
import de.btcdev.eliteanimesapp.data.services.UserService;

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
