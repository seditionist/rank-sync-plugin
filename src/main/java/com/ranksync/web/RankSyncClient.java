package com.ranksync.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ranksync.RankSyncConfig;
import com.ranksync.RankSyncPlugin;
import com.ranksync.events.KeyValidated;
import com.ranksync.models.*;
import jdk.internal.joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.eventbus.EventBus;
import okhttp3.*;
import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.util.function.Consumer;

@Slf4j
public class RankSyncClient {

    @Inject
    private OkHttpClient okHttpClient;

    private final Gson gson = new GsonBuilder()
            .setDateFormat(DateFormat.FULL, DateFormat.FULL)
            .create();

    @Inject
    private Client client;

    @Inject
    private RankSyncConfig config;

    @Inject
    private ChatMessageManager chatMessageManager;

    @Inject
    private EventBus eventBus;

    public void validateAPIKey() {
        config.keyVerified(false);
        if (Strings.isNullOrEmpty(config.apiKey()))
            return;

        ValidateKey payload = new ValidateKey(config.apiKey());
        Request request = createRequest(payload, HttpMethod.POST, "key", "validate");
        sendRequest(request, this::validateAPIKeyCallBack);
    }

    private void validateAPIKeyCallBack(Response response) {
        if (response.isSuccessful()) {
            boolean data = parseResponse(response, boolean.class);
            eventBus.post(new KeyValidated(data));
            return;
        }

        SyncStatus data = parseResponse(response, SyncStatus.class);
        final String message = (data != null ? data.getData() : null);
        sendResponseToChat(message, RankSyncPlugin.ERROR);
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
            log.info(body);
            return gson.fromJson(body, type);
        }
        catch (IOException e) {
            log.error("Could not read response {}", e.getMessage());
            return null;
        }
    }

    private void sendResponseToChat(String message, Color color) {
        ChatMessageBuilder cmb = new ChatMessageBuilder();
        cmb.append(color, "Rank-Sync: " + message);

        chatMessageManager.queue(QueuedMessage.builder()
                .type(ChatMessageType.CONSOLE)
                .runeLiteFormattedMessage(cmb.build())
                .build());
    }
}
