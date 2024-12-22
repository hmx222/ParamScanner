package com.kiwi.main;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import burp.api.montoya.http.handler.*;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kiwi.HttpRequestResponseCallback.ResponseCallBack;
import com.kiwi.Utils.KiwiUtils;
import com.kiwi.Utils.historyExtractUtils;

import static com.kiwi.main.Main.api;

public class HttpHandler implements burp.api.montoya.http.handler.HttpHandler {
    int count = 0;

    /**
     * @param requestToBeSent information about the HTTP request that is going to be sent.
     * 当有30个请求时，将urlParamMap发送到本地
     */

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        count++;
        List<ProxyHttpRequestResponse> proxyHttpRequestResponses = api.proxy().history();
        List<Map<String,Object>> ready2Send = new ArrayList<>();
        if (count %30 == 0) {
            try {
                    for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponses) {
                        ready2Send.add(KiwiUtils.covertHttpRequest2Map(proxyHttpRequestResponse.request(),
                                historyExtractUtils.getPathFromHistory(proxyHttpRequestResponses),
                                historyExtractUtils.getParamFromHistory(proxyHttpRequestResponses)));
                        TimingData timingData = proxyHttpRequestResponse.timingData();

                    }
                // Map<String, Object> paramFromHistory = historyExtract.getParamFromHistory(proxyHttpRequestResponses);
                // api.logging().logToOutput(KiwiUtils.list2BeautifulJson(ready2Send));
                KiwiUtils.sendData2Local("Kiwi:::"+KiwiUtils.list2BeautifulJson(ready2Send),new ResponseCallBack());
            } catch (MalformedURLException | URISyntaxException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    /**
     *
     * @param responseReceived information about HTTP response that was received.
     * 当有25个响应时，将urlParamMap发送到本地
     *
     */

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        count++;
        if (count % 25 == 0) {
            try {
                KiwiUtils.sendData2Local(KiwiUtils.map2json(historyExtractUtils.getPathFromHistory(api.proxy().history())),new ResponseCallBack());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return ResponseReceivedAction.continueWith(responseReceived);
    }

}
