package com.kiwi.main;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import burp.api.montoya.http.handler.*;


public class HttpHandler implements burp.api.montoya.http.handler.HttpHandler {
    public static TableModel tableModel;
    public static HttpDataStructure httpDataStructure = new HttpDataStructure();
    public HttpHandler(TableModel tableModel) {
        HttpHandler.tableModel = tableModel;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        Map<URLParse, List<Map<String, String>>> urlParamMap = new HashMap<>();
        if (requestToBeSent.hasParameters()){
            // 将参数存储在 urlList 中
            if (!httpDataStructure.addElement(urlParamMap)){
                return RequestToBeSentAction.continueWith(requestToBeSent);
            }
            try {
                httpDataStructure.setData((KiwiUtils.getParamByGET(requestToBeSent)));
                httpDataStructure.setData((KiwiUtils.getParamByPOST(requestToBeSent)));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
            }
        }
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        return ResponseReceivedAction.continueWith(responseReceived);
    }

}
