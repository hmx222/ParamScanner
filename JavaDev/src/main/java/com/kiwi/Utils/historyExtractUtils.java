package com.kiwi.Utils;

import burp.api.montoya.http.handler.TimingData;
import burp.api.montoya.http.message.MimeType;
import burp.api.montoya.proxy.ProxyHttpRequestResponse;
import com.kiwi.main.URLParse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kiwi.Utils.KiwiUtils.getPath;

public class historyExtractUtils {

    /**
     * 从历史记录中提取路径
     * @param proxyHttpRequestResponseList
     * @return 返回一个Map，key为路径，value为路径对应的参数
     * @throws MalformedURLException
     */
// 全局变量，维护最后处理的时间
    private static ZonedDateTime lastProcessedTime = ZonedDateTime.of(2020, 1, 1, 9, 0, 0, 0, ZoneId.of("Asia/Shanghai"));
    private static ZonedDateTime lastProcessedTime2 = ZonedDateTime.of(2020, 1, 1, 9, 0, 0, 0, ZoneId.of("Asia/Shanghai"));


    public static Map<String, Object> getPathFromHistory(List<ProxyHttpRequestResponse> proxyHttpRequestResponseList)
            throws MalformedURLException, URISyntaxException {
        HashMap<String, Object> urlMap = new HashMap<>();
        ZonedDateTime currentMaxTime = lastProcessedTime;

        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
            if ((proxyHttpRequestResponse.mimeType().equals(MimeType.HTML)
                    || proxyHttpRequestResponse.mimeType().equals(MimeType.SCRIPT))
                    && proxyHttpRequestResponse.response() != null) {

                // 获取请求的时间戳
                TimingData timingData = proxyHttpRequestResponse.timingData();
                ZonedDateTime requestTime = timingData.timeRequestSent();

                // 如果记录的时间早于等于最后处理时间，跳过处理
                if (!requestTime.isAfter(lastProcessedTime)) {
                    continue;
                }

                // 更新本次最大时间
                if (requestTime.isAfter(currentMaxTime)) {
                    currentMaxTime = requestTime;
                }

                // 解析URL并处理路径数据
                URLParse urlParse = new URLParse(new URL(proxyHttpRequestResponse.request().url()));
                List<String> paths = KiwiUtils.dataClean(urlParse.getUrl(), getPath(proxyHttpRequestResponse.response().bodyToString()));
                urlMap.put(urlParse.getUrlWithoutPath(), paths);
            }
        }

        // 更新全局最后处理时间
        lastProcessedTime = currentMaxTime;
        return urlMap;
    }

    public static Map<String, Object> getParamFromHistory(List<ProxyHttpRequestResponse> proxyHttpRequestResponseList)
            throws MalformedURLException {
        HashMap<String, Object> urlMap = new HashMap<>();
        Map<String, Object> params;
        ZonedDateTime currentMaxTime = lastProcessedTime2;

        for (ProxyHttpRequestResponse proxyHttpRequestResponse : proxyHttpRequestResponseList) {
            // 获取请求时间
            TimingData timingData = proxyHttpRequestResponse.timingData();
            ZonedDateTime requestTime = timingData.timeRequestSent();

            // 如果记录的时间早于等于最后处理时间，跳过处理
            if (!requestTime.isAfter(lastProcessedTime2)) {
                continue;
            }

            // 更新本次最大时间
            if (requestTime.isAfter(currentMaxTime)) {
                currentMaxTime = requestTime;
            }

            // 检查是否有参数或者请求体
            if (proxyHttpRequestResponse.request().hasParameters()
                    || (proxyHttpRequestResponse.request().bodyToString() != null)) {

                URLParse urlParse = new URLParse(new URL(proxyHttpRequestResponse.request().url()));

                // 根据请求方法解析参数
                if (proxyHttpRequestResponse.request().method().equals("GET")) {
                    params = KiwiUtils.getParamByGET(proxyHttpRequestResponse.request());
                } else {
                    params = KiwiUtils.getParamByPOST(proxyHttpRequestResponse.request());
                }

                // 将 URL 和参数映射到 Map
                urlMap.put(urlParse.getUrlWithoutPath(), params);
            }
        }

        // 更新全局最后处理时间
        lastProcessedTime2 = currentMaxTime;
        return urlMap;
    }


}
