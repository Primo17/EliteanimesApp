package de.btcdev.eliteanimesapp;

import javax.inject.Singleton;

import dagger.Component;
import de.btcdev.eliteanimesapp.ui.activities.AccountSettingsActivity;
import de.btcdev.eliteanimesapp.ui.activities.AnimeListActivity;
import de.btcdev.eliteanimesapp.ui.activities.BoardActivity;
import de.btcdev.eliteanimesapp.ui.activities.CommentActivity;
import de.btcdev.eliteanimesapp.ui.activities.FriendActivity;
import de.btcdev.eliteanimesapp.ui.activities.InfoActivity;
import de.btcdev.eliteanimesapp.ui.activities.LoginActivity;
import de.btcdev.eliteanimesapp.ui.activities.NewCommentActivity;
import de.btcdev.eliteanimesapp.ui.activities.NewPostActivity;
import de.btcdev.eliteanimesapp.ui.activities.NewPrivateMessageActivity;
import de.btcdev.eliteanimesapp.ui.activities.PostActivity;
import de.btcdev.eliteanimesapp.ui.activities.PrivateMessageActivity;
import de.btcdev.eliteanimesapp.ui.activities.ProfileActivity;
import de.btcdev.eliteanimesapp.ui.activities.ProfileDescriptionActivity;
import de.btcdev.eliteanimesapp.ui.activities.SearchActivity;
import de.btcdev.eliteanimesapp.ui.activities.SettingsActivity;
import de.btcdev.eliteanimesapp.ui.activities.ThreadActivity;
import de.btcdev.eliteanimesapp.ui.activities.UserProfileActivity;

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
    void inject(PostActivity activity);
    void inject(PrivateMessageActivity activity);
    void inject(ProfileActivity activity);
    void inject(ProfileDescriptionActivity activity);
    void inject(SearchActivity activity);
    void inject(SettingsActivity activity);
    void inject(ThreadActivity activity);
    void inject(UserProfileActivity activity);
    void inject(EaApp app);

    //TODO: check if ParentActivity really does not need the injection itself
    //void inject(ParentActivity activity);

    //TODO: not exactly the best way to inject it to a dialog...
    void inject(AccountSettingsActivity.BlockedUsersDialog dialog);
    void inject(AnimeListActivity.AnimeRatingDialogFragment dialog);
}
