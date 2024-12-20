package com.kiwi.main;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.util.List;
import static com.kiwi.main.Main.api;

public class ResponseCallBack implements HttpRequestResponseCallback{

    @Override
    public void onSuccess(List<HttpRequestResponse> response) {
        for (HttpRequestResponse resp : response) {
            HttpHandler.tableModel.add(resp);
        }
    }

    @Override
    public void onError(String errorMessage) {
        api.logging().logToOutput(errorMessage);
    }
}
