package com.ranksync.web;

import java.io.IOException;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class RankSyncCallback implements Callback {

    @Override
    @EverythingIsNonNull
    public void onFailure(Call call, IOException e) {
        if (failureConsumer == null)
            log.warn("Error submitting request, caused by {}.", e.getMessage());
        else
            failureConsumer.accept(e);
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call call, Response response) {
        try {
            responseConsumer.accept(response);
        }
        catch (Throwable e) {
            log.warn("Error when handling response, caused by {}.", e.getMessage());
        }
        finally {
            response.close();
        }
    }

    private final Consumer<Response> responseConsumer;
    private Consumer<Exception> failureConsumer;
}
