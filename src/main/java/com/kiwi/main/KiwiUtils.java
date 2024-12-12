package com.kiwi.main;

import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.message.ContentType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.params.HttpParameter;
import burp.api.montoya.http.message.params.HttpParameterType;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static com.kiwi.main.Main.api;
import static com.kiwi.main.Main.backList;

public final class KiwiUtils {

    private KiwiUtils() {
    }

    public static void sendHttpRequestAsync(String url, List<Map<String, String>> paramMap, HttpRequestResponseCallback callback,String method) {
        SwingWorker<List<HttpRequestResponse>, Void> worker = new SwingWorker<>() {
            private List<HttpRequestResponse> httpRequestResponses = new ArrayList<>();
            private String errorMessage;

            @Override
            protected List<HttpRequestResponse> doInBackground() {
                try {
                    URLParse urlParse = new URLParse(new URL(url));
                    if (method.equals("GET")) {
                        for (Map<String, String> map : paramMap) {
                            HttpRequest httpRequest = HttpRequest.httpRequestFromUrl(urlParse.getUrl()).
                                    withMethod(method).withAddedParameters(Map2HttpParameter(map));
                            HttpRequestResponse response = api.http().sendRequest(httpRequest);

                            httpRequestResponses.add(response);
                        }
                    } else if (method.equals("POST")){
                        for (Map<String, String> map : paramMap) {
                            for (Map.Entry<String, String> entry : map.entrySet()) {
                                HttpRequest httpRequest = HttpRequest.httpRequestFromUrl(urlParse.getUrl()).
                                        withMethod(method).withBody(entry.toString());
                                //api.logging().logToOutput("entry.toString():"+entry.toString());
                                HttpRequestResponse response = api.http().sendRequest(httpRequest);
                                httpRequestResponses.add(response);
                            }
                        }
                    }
                    return httpRequestResponses;
                } catch (Exception ex) {
                    errorMessage = "Request failed: " + ex.getMessage();
                    return null;
                }
            }

            @Override
            protected void done() {
                if (httpRequestResponses != null && !httpRequestResponses.isEmpty()) {
                    callback.onSuccess(httpRequestResponses);
                } else {
                    callback.onError(errorMessage);
                }
            }
        };
        worker.execute();
    }

    public static List<HttpParameter> Map2HttpParameter(Map<String, String> map) {
        List<HttpParameter> params = new ArrayList<>();
        // 遍历 map 的键值对
        for (Map.Entry<String, String> entry : map.entrySet()) {
            params.add(new HttpParameter() {
                @Override
                public HttpParameterType type() {
                    return HttpParameterType.URL;
                }

                @Override
                public String name() {
                    return entry.getKey();
                }

                @Override
                public String value() {
                    return entry.getValue();
                }
            });
        }
        return params;
    }


    public static Map<URLParse, List<Map<String, String>>> getParamByPOST(HttpRequestToBeSent httpRequestToBeSent) throws MalformedURLException {
        Map<URLParse, List<Map<String, String>>> urlParamMap = new HashMap<>();
        List<Map<String, String>> paramList = new ArrayList<>();
        URLParse urlParse = new URLParse(new URL(httpRequestToBeSent.url()));
        if (!backList.contains(urlParse.getHost())) {
            // 获取请求的路径
            String postParam = httpRequestToBeSent.bodyToString();
            if (!postParam.isEmpty()) {
                if (httpRequestToBeSent.contentType().equals(ContentType.URL_ENCODED)) {
                    // 按 & 拆分参数对
                    String[] paramPairs = postParam.split("&");
                        for (String paramPair : paramPairs) {
                            // 按 = 拆分参数名和参数值
                            String[] param = paramPair.split("=");
                            if (param.length == 2) {
                                // 创建存储参数的 HashMap
                                Map<String, String> paramMap = new HashMap<>();
                                paramMap.put(param[0], param[1]);
                                paramList.add(paramMap);
                            }
                        }
                        urlParamMap.put(urlParse, paramList);
                } else if (httpRequestToBeSent.contentType().equals(ContentType.JSON)) {
                    // 调用 parseJsonToMap 方法将 JSON 字符串转换为 Map
                    Map<String, String> paramMap = parseJsonToMap(postParam);
                    api.logging().logToOutput("paramMap: " + paramMap);
                    paramList.add(paramMap);
                    urlParamMap.put(urlParse, paramList);
                }
            }
        }
        return urlParamMap;
    }


    public static Map<URLParse, List<Map<String, String>>> getParamByGET(HttpRequestToBeSent httpRequestToBeSent)
            throws MalformedURLException {
        Map<URLParse, List<Map<String, String>>> urlParamMap = new HashMap<>();
            // 获取请求的 url 并存储
            URLParse urlParse = new URLParse(new URL(httpRequestToBeSent.url()));
            if (!backList.contains(urlParse.getHost())){
                List<Map<String, String>> paramList = new ArrayList<>();
                // 获取请求的路径
                String query = httpRequestToBeSent.path();
                if (query!= null &&!query.isEmpty()) {
                    // 提取? 后的参数部分
                    String queryPart = query.contains("?")? query.split("\\?")[1] : "";
                    // 按 & 拆分参数对
                    String[] paramPairs = queryPart.split("&");
                    for (String paramPair : paramPairs) {
                        // 按 = 拆分参数名和参数值
                        String[] param = paramPair.split("=");
                        if (param.length == 2) {
                            // 创建存储参数的 HashMap
                            Map<String, String> paramMap = new HashMap<>();
                            paramMap.put(param[0], param[1]);
                            paramList.add(paramMap);
                        }
                    }
                    // 将参数列表存储在 urlParamMap 中，以 url 为键
                    urlParamMap.put(urlParse, paramList);
                }
            }
        return urlParamMap;
    }

    public static List<String> readFileToList(String filePath) {
        List<String> contentList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine())!= null) {
                contentList.add(line);
            }
        } catch (IOException e) {
            e.toString();
        }
        return contentList;
    }

    public static String getTitleFromResponse(HttpRequestResponse httpRequestResponse) {
        String responseBody = httpRequestResponse.response().bodyToString();
        int startIndex = responseBody.indexOf("<title>");
        if (startIndex != -1) {
            int endIndex = responseBody.indexOf("</title>");
            if (endIndex != -1) {
                return responseBody.substring(startIndex + 7, endIndex);
            }
        }
        return null;
    }


    public static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(json);
            parseNode(rootNode, "", resultMap);
        } catch (Exception e) {
            api.logging().logToError(e.toString());
        }
        return resultMap;
    }

    private static void parseNode(JsonNode node, String currentPath, Map<String, String> resultMap) {
        if (node.isObject()) {
            // 如果当前节点是对象，遍历其子节点
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                parseNode(value, currentPath.isEmpty() ? key : currentPath + "." + key, resultMap);
            }
        } else {
            // 如果是最终节点，将路径和值加入 Map
            resultMap.put(currentPath, node.asText());
        }
    }


}
