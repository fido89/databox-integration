package databox.facebook;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import databox.auth.AuthUser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class FacebookService {

    private OAuth20Service service;

    private FacebookService() {
    }

    public static FacebookService getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void init(final String clientId, final String clientSecret) {
        service = new ServiceBuilder(clientId)
                .apiSecret(clientSecret)
                .callback("https://postman-echo.com/get")
                .build(FacebookApi.customVersion("8.0"));
    }

    public AuthUser authorize() throws IOException, ExecutionException, InterruptedException {
        final String secretState = "secret" + new Random().nextInt(999_999);

        final Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("=== Facebook's OAuth Workflow ===");
        System.out.println();

        // Obtain the Authorization URL
        final String authorizationUrl = service.getAuthorizationUrl(secretState);
        System.out.println("Open and authorize Databox-integration here:");
        System.out.println(authorizationUrl);
        System.out.println("And paste the authorization code here");
        System.out.print(">>");
        final String code = in.nextLine();
        System.out.println();

        System.out.println("And paste the state from server here.");
        System.out.print(">>");
        final String value = in.nextLine();
        if (!secretState.equals(value)) {
            System.out.println("Ooops, state value does not match!");
            System.out.println("Expected = " + secretState);
            System.out.println("Got      = " + value);
            System.out.println();
            System.exit(0);
        }

        final OAuth2AccessToken accessToken = service.getAccessToken(code);
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/v8.0/me");

        JsonObject response = getServerResponse(accessToken.getAccessToken(), request);
        return new AuthUser(response.get("id").getAsString(), accessToken.getAccessToken());
    }

    public int getFriendsCount(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/v8.0/" + authUser.getUserId() + "/friends?fields=summary");
        JsonObject response = getServerResponse(authUser.getAccessToken(), request);
        return response.get("summary") != null ? response.get("summary").getAsJsonObject().get("total_count").getAsInt() : 0;
    }

    public int getPageLikes(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/v8.0/" + authUser.getUserId() + "/likes?fields=summary");
        JsonObject response = getServerResponse(authUser.getAccessToken(), request);
        return response.get("summary") != null ? response.get("summary").getAsJsonObject().get("total_count").getAsInt() : 0;
    }

    public int getUploadedPhotos(AuthUser authUser) throws InterruptedException, ExecutionException, IOException {
        final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/v8.0" + authUser.getUserId() + "/photos?type=uploaded");
        JsonObject response = getServerResponse(authUser.getAccessToken(), request);
        // TODO
        return 14;
    }

    private JsonObject getServerResponse(String accessToken, OAuthRequest request) throws InterruptedException, ExecutionException, IOException {
        service.signRequest(accessToken, request);
        try (Response response = service.execute(request)) {
            return JsonParser.parseString(response.getBody()).getAsJsonObject();
        }
    }

    private static class SingletonHolder {
        private static final FacebookService INSTANCE = new FacebookService();
    }
}
