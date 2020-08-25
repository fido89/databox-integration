package databox.twitter;

import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import databox.auth.AuthUser;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class TwitterService {

    private OAuth10aService service;

    private TwitterService() {
    }

    public static TwitterService getInstance() {
        return TwitterService.SingletonHolder.INSTANCE;
    }

    public void init(final String apiKey, final String apiSecret) {
        service = new ServiceBuilder(apiKey)
                .apiSecret(apiSecret)
                .build(TwitterApi.instance());
    }

    public AuthUser authorize() throws IOException, ExecutionException, InterruptedException {
        final Scanner in = new Scanner(System.in);

        System.out.println("=== Twitter's OAuth Workflow ===");
        System.out.println();

        // Obtain the Request Token
        final OAuth1RequestToken requestToken = service.getRequestToken();

        System.out.println("Open and authorize Databox-integration here:");
        System.out.println(service.getAuthorizationUrl(requestToken));
        System.out.println("And paste the verifier here");
        System.out.print(">>");
        final String oauthVerifier = in.nextLine();
        System.out.println();

        // Trade the Request Token and Verfier for the Access Token
        final OAuth1AccessToken accessToken = service.getAccessToken(requestToken, oauthVerifier);

        JsonObject accountInfo = getAccountInformation(accessToken);
        return new AuthUser(accountInfo.get("id").getAsString(), accessToken);
    }

    private JsonObject getAccountInformation(OAuth1AccessToken accessToken) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/account/verify_credentials.json");
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            return JsonParser.parseString(response.getBody()).getAsJsonObject();
        }
    }

    public int getFollowersCount(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        JsonObject accountInfo = getAccountInformation(authUser.getTwitterAccessToken());
        return accountInfo.get("followers_count") != null ? accountInfo.get("followers_count").getAsInt() : 0;
    }

    public int getFriends(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        JsonObject accountInfo = getAccountInformation(authUser.getTwitterAccessToken());
        return accountInfo.get("friends_count") != null ? accountInfo.get("friends_count").getAsInt() : 0;
    }

    public int getTweets(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        JsonObject accountInfo = getAccountInformation(authUser.getTwitterAccessToken());
        return accountInfo.get("statuses_count") != null ? accountInfo.get("statuses_count").getAsInt() : 0;
    }

    public int getLikes(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        JsonObject accountInfo = getAccountInformation(authUser.getTwitterAccessToken());
        return accountInfo.get("favourites_count") != null ? accountInfo.get("favourites_count").getAsInt() : 0;
    }

    private static class SingletonHolder {
        private static final TwitterService INSTANCE = new TwitterService();
    }
}
