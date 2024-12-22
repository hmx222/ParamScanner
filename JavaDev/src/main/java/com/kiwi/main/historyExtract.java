package com.kiwi.main;

import burp.api.montoya.http.message.MimeType;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kiwi.main.KiwiUtils.getPath;

public class historyExtract {

    /**
     * 从历史记录中提取路径
     * @param proxyHttpRequestResponseList
     * @return 返回一个Map，key为路径，value为路径对应的参数
     * @throws MalformedURLException
     */

    public static Map<String, Object> getPathFromHistory(List<ProxyHttpRequestResponse> proxyHttpRequestResponseList) throws MalformedURLException, URISyntaxException {
        // List<ProxyHttpRequestResponse> effectiveDataFiltering = new ArrayList<ProxyHttpRequestResponse>();
        HashMap <String,Object> urlMap = new HashMap<>();
        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
            if ((proxyHttpRequestResponse.mimeType().equals(MimeType.HTML)
                    || proxyHttpRequestResponse.mimeType().equals(MimeType.SCRIPT))
                    && proxyHttpRequestResponse.response() != null) {
                // TODO 利用此来进行分开每次的请求 proxyHttpRequestResponse.timingData()
                URLParse urlParse = new URLParse(new URL(proxyHttpRequestResponse.request().url()));
                List<String> paths = KiwiUtils.dataClean(urlParse.getUrl(),getPath(proxyHttpRequestResponse.response().bodyToString()));
                urlMap.put(urlParse.getUrlWithoutPath(),paths);
            }
        }
        return urlMap;
    }

    public static Map<String, Object> getParamFromHistory(List<ProxyHttpRequestResponse> proxyHttpRequestResponseList) throws MalformedURLException {
        HashMap <String,Object> urlMap = new HashMap<>();
        Map<String, Object> params;
        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
            if (proxyHttpRequestResponse.request().hasParameters() || (proxyHttpRequestResponse.request().bodyToString() != null)){
                URLParse urlParse = new URLParse(new URL(proxyHttpRequestResponse.request().url()));
                if (proxyHttpRequestResponse.request().method().equals("GET")){
                    params = KiwiUtils.getParamByGET(proxyHttpRequestResponse.request());
                }else {
                    params = KiwiUtils.getParamByPOST(proxyHttpRequestResponse.request());
                }
                urlMap.put(urlParse.getUrlWithoutPath(),params);
            }
        }
        return urlMap;
    }


}
