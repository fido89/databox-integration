package databox.auth;

import com.github.scribejava.core.model.OAuth1AccessToken;

public class AuthUser {
    private String userId;
    private String accessToken;
    private OAuth1AccessToken twitterAccessToken;

    public AuthUser(String userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    public AuthUser(String userId, OAuth1AccessToken twitterAccessToken) {
        this.userId = userId;
        this.twitterAccessToken = twitterAccessToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public OAuth1AccessToken getTwitterAccessToken() {
        return twitterAccessToken;
    }
}
