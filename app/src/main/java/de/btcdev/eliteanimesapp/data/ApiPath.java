package de.btcdev.eliteanimesapp.data;

public class ApiPath {
    public static final String BASE_URL = "http://www.eliteanimes.com";

    // LOGIN
    public static final String LOGIN = "/api/login";
    public static final String LOGOUT = "/api/logout";
    public static final String BOARD_TOKEN = "/api/getToken";

    // PROFILE AND USER
    public static final String NEWS = "/api/getUserUpdates";
    public static final String PROFILE = "/api/getProfil";
    public static final String PROFILE_DESCRIPTION = "/api/getProfilDescription";
    public static final String USER_SEARCH = "/api/searchUser";
    public static final String USER_BLOCK = "/blockuser.php";
    public static final String USERS_BLOCKED = "/api/getBlockedUser";

    // FRIEND
    public static final String FRIEND_ADD = "/friend/add/";
    public static final String FRIEND_DELETE = "/friend/delete/";
    public static final String FRIENDS = "/api/getFriendList";
    public static final String FRIEND_REQUEST_ACCEPT = "/friend/accept/";
    public static final String FRIEND_REQUEST_DECLINE = "/friend/decline/";
    public static final String FRIEND_REQUESTS = "/api/getFriendRequests";

    // COMMENT
    public static final String COMMENT_ADD = "/profil/";
    public static final String COMMENT_EDIT = "/profil/";
    public static final String COMMENT_DELETE = "/commentdelete.php";
    public static final String COMMENTS = "/api/getComments";

    // PRIVATE MESSAGE
    public static final String PM = "/api/getPM";
    public static final String PM_ANSWER = "/api/answerPM";
    public static final String PM_SEND = "/api/sendPM";
    public static final String PM_DELETE = "/api/deletePM/";
    public static final String PMS = "/api/getInboxPMs";

    // BOARD
    public static final String BOARD = "/api/getForum";
    public static final String BOARDS = "/api/getForums";
    public static final String BOARD_POST = "/api/getForumPost";
    public static final String BOARD_POST_ADD = "/api/addForumPost";
    public static final String BOARD_POST_EDIT = "/api/editForumPost";
    public static final String BOARD_POST_DELETE = "/api/deleteForumPost";
    public static final String BOARD_POSTS = "/api/getForumThread";
    public static final String BOARD_STATISTICS = "/api/getStatistics";

    // ANIME
    public static final String ANIME_LIST = "/api/getAnimelist";
    public static final String ANIME_RATE = "/animelist/";

}
