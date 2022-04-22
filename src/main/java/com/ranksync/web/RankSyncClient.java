package com.ranksync.web;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ranksync.RankSyncConfig;
import com.ranksync.events.ErrorReceived;
import com.ranksync.events.KeyValidated;
import com.ranksync.events.MembersSynced;
import com.ranksync.events.RanksSynced;
import com.ranksync.models.*;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.EventBus;
import okhttp3.*;
import javax.inject.Inject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.function.Consumer;

@Slf4j
public class RankSyncClient {

    @Inject
    private OkHttpClient okHttpClient;

    private final Gson gson = new GsonBuilder()
            .setDateFormat(DateFormat.FULL, DateFormat.FULL)
            .create();

    @Inject
    private RankSyncConfig config;

    @Inject
    private EventBus eventBus;

    public void syncClanMembers(String name, ArrayList<MemberImport> clanMembers) {
        MembersImport payload = new MembersImport(config.apiKey(), name, clanMembers);
        Request request = createRequest(payload, HttpMethod.PUT, "import", "members");
        sendRequest(request, r -> responseCallBack(r, MembersSynced.class, "Error syncing members. Try again later."));
    }

    public void syncClanRanks(String name, ArrayList<RankImport> clanRanks) {
        RanksImport payload = new RanksImport(config.apiKey(), name, clanRanks);
        Request request = createRequest(payload, HttpMethod.PUT, "import", "ranks");
        sendRequest(request, r -> responseCallBack(r, RanksSynced.class, "Error syncing ranks. Try again later."));
    }

    public void validateAPIKey(String name) {
        config.keyVerified(false);
        String key = config.apiKey();
        if (Strings.isNullOrEmpty(key))
            return;

        ValidateKey payload = new ValidateKey(key, name);
        Request request = createRequest(payload, HttpMethod.POST, "key", "validate");
        sendRequest(request, r -> responseCallBack(r, KeyValidated.class, "Error validating key. Try again later."));
    }

    void sendRequest(Request request, Consumer<Response> consumer) {
        sendRequest(request, new RankSyncCallback(consumer));
    }

    void sendRequest(Request request, Callback callback) {
        okHttpClient.newCall(request).enqueue(callback);
    }

    private Request createRequest(Object payload, HttpMethod httpMethod, String... pathSegments) {
        HttpUrl url = buildUrl(pathSegments);
        RequestBody body = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                gson.toJson(payload)
        );

        Request.Builder requestBuilder = new Request.Builder()
                .header("User-Agent", "Rank-Sync RuneLite Plugin")
                .url(url);

        switch (httpMethod) {
            case PUT:
                return requestBuilder.put(body).build();

            case POST:
                return requestBuilder.post(body).build();

            default:
                throw new UnsupportedOperationException();
        }
    }

    private HttpUrl buildUrl(String[] pathSegments) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(5000)
                .addPathSegments("api");

        for (String pathSegment : pathSegments)
            urlBuilder.addPathSegment(pathSegment);

        return urlBuilder.build();
    }

    private <T> T parseResponse(Response r, Class<T> type) {
        try {
            String body = r.body() != null ? r.body().string() : null;
            return gson.fromJson(body, type);
        }
        catch (IOException e) {
            log.error("Could not read response {}", e.getMessage());
            return null;
        }
    }

    private void parseError(Response response, String defaultMessage) {
        ErrorReceived data = parseResponse(response, ErrorReceived.class);
        eventBus.post(data != null ? data : defaultMessage);
    }

    private <T> void responseCallBack(Response response, Class<T> type, String errorMessage) {
        if (!response.isSuccessful()) {
            parseError(response, errorMessage);
            return;
        }

        T data = parseResponse(response, type);
        if (data != null) {
            eventBus.post(data);
            return;
        }

        eventBus.post(new ErrorReceived(errorMessage));
    }
}
