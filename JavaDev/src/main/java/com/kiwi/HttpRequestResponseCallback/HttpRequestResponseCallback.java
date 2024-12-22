package com.kiwi.HttpRequestResponseCallback;

import burp.api.montoya.http.message.HttpRequestResponse;

import java.util.List;

public interface HttpRequestResponseCallback {
    void onSuccess(List<HttpRequestResponse> response);
    void onError(String errorMessage);
}
