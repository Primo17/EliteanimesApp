package de.btcdev.eliteanimesapp;

import javax.inject.Singleton;

import dagger.Component;
import de.btcdev.eliteanimesapp.gui.AccountSettingsActivity;
import de.btcdev.eliteanimesapp.gui.AnimeListActivity;
import de.btcdev.eliteanimesapp.gui.BoardActivity;
import de.btcdev.eliteanimesapp.gui.CommentActivity;
import de.btcdev.eliteanimesapp.gui.FriendActivity;
import de.btcdev.eliteanimesapp.gui.InfoActivity;
import de.btcdev.eliteanimesapp.gui.LoginActivity;
import de.btcdev.eliteanimesapp.gui.NewCommentActivity;
import de.btcdev.eliteanimesapp.gui.NewPostActivity;
import de.btcdev.eliteanimesapp.gui.NewPrivateMessageActivity;
import de.btcdev.eliteanimesapp.gui.ParentActivity;
import de.btcdev.eliteanimesapp.gui.PostActivity;
import de.btcdev.eliteanimesapp.gui.PrivateMessageActivity;
import de.btcdev.eliteanimesapp.gui.ProfileActivity;
import de.btcdev.eliteanimesapp.gui.ProfileDescriptionActivity;
import de.btcdev.eliteanimesapp.gui.SearchActivity;
import de.btcdev.eliteanimesapp.gui.SettingsActivity;
import de.btcdev.eliteanimesapp.gui.ThreadActivity;
import de.btcdev.eliteanimesapp.gui.UserProfileActivity;

@Singleton
@Component(modules = {AppModule.class})
public interface EaComponent {
    void inject(AccountSettingsActivity activity);
    void inject(AnimeListActivity activity);
    void inject(BoardActivity activity);
    void inject(CommentActivity activity);
    void inject(FriendActivity activity);
    void inject(InfoActivity activity);
    void inject(LoginActivity activity);
    void inject(NewCommentActivity activity);
    void inject(NewPostActivity activity);
    void inject(NewPrivateMessageActivity activity);
    void inject(ParentActivity activity);
    void inject(PostActivity activity);
    void inject(PrivateMessageActivity activity);
    void inject(ProfileActivity activity);
    void inject(ProfileDescriptionActivity activity);
    void inject(SearchActivity activity);
    void inject(SettingsActivity activity);
    void inject(ThreadActivity activity);
    void inject(UserProfileActivity activity);
    void inject(EaApp app);

    //TODO: not exactly the best way to inject it to a dialog...
    void inject(AccountSettingsActivity.BlockedUsersDialog dialog);
}
