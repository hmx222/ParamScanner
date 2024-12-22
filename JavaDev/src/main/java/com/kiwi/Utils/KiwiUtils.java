package com.kiwi.Utils;

import burp.api.montoya.http.message.ContentType;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kiwi.HttpRequestResponseCallback.HttpRequestResponseCallback;
import com.kiwi.main.URLParse;

import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.kiwi.main.Main.api;
import static com.kiwi.main.Main.backList;

public final class KiwiUtils {

    private KiwiUtils() {
    }


    public static Map<String, Object> getParamByPOST(HttpRequest httpRequest) throws MalformedURLException {
        Map<String, Object> urlParamMap = new HashMap<>();
        List<Map<String, String>> paramList = new ArrayList<>();
        URLParse urlParse = new URLParse(new URL(httpRequest.url()));
        if (!backList.contains(urlParse.getHost())) {
            // 获取请求的路径
            String postParam = httpRequest.bodyToString();
            if (!postParam.isEmpty()) {
                if (httpRequest.contentType().equals(ContentType.URL_ENCODED)) {
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
                        urlParamMap.put(urlParse.getUrlWithoutPath(), paramList);
                } else if (httpRequest.contentType().equals(ContentType.JSON)) {
                    // 调用 parseJsonToMap 方法将 JSON 字符串转换为 Map
                    Map<String, String> paramMap = parseJsonToMap(postParam);
                    // api.logging().logToOutput("paramMap: " + paramMap);
                    paramList.add(paramMap);
                    urlParamMap.put(urlParse.getUrlWithoutPath(), paramList);
                }
            }
        }
        return urlParamMap;
    }


    public static Map<String, Object> getParamByGET(HttpRequest httpRequest)
            throws MalformedURLException {
        Map<String, Object> urlParamMap = new HashMap<>();
            // 获取请求的 url 并存储
            URLParse urlParse = new URLParse(new URL(httpRequest.url()));
            if (!backList.contains(urlParse.getHost())){
                List<Map<String, String>> paramList = new ArrayList<>();
                // 获取请求的路径
                String query = httpRequest.path();
                if (query!= null &&!query.isEmpty()) {
                    // 提取? 后的参数部分
                    String queryPart = query.contains("?")? query.split("\\?")[1] : "";
                    // api.logging().logToOutput("queryPart = " + queryPart);
                    // 按 & 拆分参数对
                    String[] paramPairs = queryPart.split("&");
                    // api.logging().logToOutput("paramPairs.length = " + paramPairs.length);
                    for (String paramPair : paramPairs) {
                        // 按 = 拆分参数名和参数值
                        String[] param = paramPair.split("=");
                       //  api.logging().logToOutput("param = " + param[0]);
                        if (param.length == 2) {
                            // 创建存储参数的 HashMap
                            Map<String, String> paramMap = new HashMap<>();
                            paramMap.put(param[0], param[1]);
                            paramList.add(paramMap);
                        }
                    }
                    // 将参数列表存储在 urlParamMap 中，以 url 为键
                    urlParamMap.put(urlParse.getUrlWithoutPath(), paramList);
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


    public static List<String> getPath(String body) {
        // 定义正则表达式字符串，注意这里转义字符等要符合Java语法规范
        String patternRaw =
                "(?:\"|\')(" +
                        "((?:[a-zA-Z]{1,10}://|//)" + // 匹配协议 [a-zA-Z]*1-10 或 //
                        "[^\"/]{1,}\\." + // 匹配域名（字符 + 点）
                        "[a-zA-Z]{2,}(?!png|css|jpeg|mp4|mp3|gif|ico)[^\"']{0,})" + // 匹配域名后缀或路径，排除 png/css/jpeg/mp4/mp3
                        "|" +
                        "((?:/|\\./|\\.\\./)" + // 匹配以 /,../,./ 开头
                        "[^\"'><,;|*()%$^/\\\\\\[\\]]" + // 排除某些字符，调整转义及顺序让其更清晰准确
                        "[^\"'><,;|()]{1,})" + // 剩下的字符不能是特殊符号
                        "|" +
                        "([a-zA-Z0-9_\\-/]{1,}/" + // 匹配相对路径（/）
                        "[a-zA-Z0-9_\\-/]{1,}" + // 资源名称
                        "\\.(?:[a-zA-Z]{1,4}|action)" + // 文件扩展名（长度 1-4 或 action）
                        "(?:[\\?|/][^\"|']{0,}|))" + // 可选的参数部分
                        "|" +
                        "([a-zA-Z0-9_\\-]{1,}" + // 文件名
                        "\\.(?:php|asp|aspx|jsp|json|action|html|js|txt|xml)" + // 扩展名
                        "(?:\\?[^\"|']{0,}|))" + // 可选的查询参数
                        ")" +
                        "(?:\"|\')"; // 结束引号

        // 编译正则表达式
        Pattern pattern = Pattern.compile(patternRaw);
        // 创建匹配器
        Matcher matcher = pattern.matcher(body);
        List<String> resultList = new ArrayList<>();
        // 查找匹配项并添加到结果列表中
        while (matcher.find()) {
            if (matcher.group(1) != null && !matcher.group(1).endsWith("css") &&
                    !matcher.group(1).endsWith("png") && !matcher.group(1).endsWith("jpeg") &&
                    !matcher.group(1).endsWith("mp4") && !matcher.group(1).endsWith("mp3") &&
                    !matcher.group(1).endsWith("gif") && !matcher.group(1).endsWith("ico")) {

                resultList.add(matcher.group(1).replace("\\","/"));
            }
        }
        return resultList;
    }

    public static List<String> dataClean(String urlWithPath,List<String> contentList) throws URISyntaxException {
        List<String> dataList = new ArrayList<>();
        for (String urlWithoutClean : contentList) {
            if (checkPathBackList(urlWithoutClean))
                continue;
            try {
                urlWithPath = urlWithPath.replace("\\","/");
                urlWithoutClean = urlWithoutClean.replace("\\","/");
                // 验证urlWithPath是否为有效的URL
                if (!isValidURL(urlWithPath)) {
                    throw new IllegalArgumentException("Invalid URL: " + urlWithPath);
                }
                // 解析输入的url，主要是用来完整的URL的拼接，这里使用Java的URI类来解析（类似Python的urlparse功能）
                URL handled_url = new URL(urlWithPath);
                // 解析http、https协议，获取协议部分（scheme）
                String Protocol = handled_url.getProtocol();
                // 解析出域名，获取主机部分（host），这里假设没有端口等复杂情况，如果有需要进一步处理
                String Domain = handled_url.getHost();
                // 解析出路径，获取路径部分（path）
                String Path = handled_url.getPath();

                if (urlWithoutClean.startsWith("/")) {
                    // 处理以斜杠开头的相对路径
                    if (urlWithoutClean.startsWith("//")) {
                        if (compareUrl(urlWithPath,Protocol + ":" + urlWithoutClean))
                            dataList.add( Protocol + ":" + urlWithoutClean);
                    } else {
                        // 此时也就是 / 开头的
                        if (compareUrl(urlWithPath,Protocol + "://" + Domain + urlWithoutClean))
                            dataList.add(Protocol + "://" + Domain + urlWithoutClean);
                    }
                } else if (urlWithoutClean.startsWith("./")) {
                    // 处理以./开头的相对路径，去掉开头的./然后拼接
                    if (compareUrl(urlWithPath,Protocol + "://" + Domain + urlWithoutClean.substring(2)))
                        dataList.add(Protocol + "://" + Domain + urlWithoutClean.substring(2));
                } else if (urlWithoutClean.startsWith("../")) {
                    // 处理以../开头的相对路径，这里借助Paths类来处理路径拼接（类似Python的os.path相关功能）
                    String joinedPath = Paths.get(Path, urlWithoutClean).normalize().toString();
                    if (compareUrl(urlWithPath,Protocol + "://" + Domain + joinedPath))
                        dataList.add( Protocol + "://" + Domain + joinedPath);
                } else if (urlWithoutClean.startsWith("http") || urlWithoutClean.startsWith("https")) {
                    // 处理以http或https开头的绝对路径，直接返回原路径
                    if (compareUrl(urlWithPath,Protocol + "://" + Domain + urlWithoutClean))
                        dataList.add(urlWithoutClean);
                } else {
                    // 处理其他情况，在域名后添加斜杠再拼接路径
                    if (compareUrl(urlWithPath,Protocol + "://" + Domain + urlWithoutClean))
                        dataList.add(Protocol + "://" + Domain + "/" + urlWithoutClean);
                }
            } catch (MalformedURLException e) {
                //api.logging().logToOutput("urlWithPath: " + urlWithPath + " urlWithoutClean: " + urlWithoutClean + " error: " + e.toString());
                api.logging().logToError(e.getMessage());
                // api.logging().logToError(e.getCause().toString());
                // api.logging().logToOutput("urlWithPath:"+ urlWithPath);
                //api.logging().logToOutput("urlWithoutClean: " + urlWithoutClean);
            }
        }
        return dataList;
    }

    public static List<String> deepFind(List<String> urlList) throws URISyntaxException {
        List<String> resultList = new ArrayList<>();
        int i = 0;
        if (!urlList.isEmpty()){
            for (String url : urlList) {
                i = i + 1;
                HttpRequest httpRequest = HttpRequest.httpRequestFromUrl(url).withMethod("GET");
                HttpRequestResponse httpRequestResponse = api.http().sendRequest(httpRequest);
                List<String> path = getPath(httpRequestResponse.response().bodyToString());
                resultList.addAll(dataClean(url, path));
                if (i % 3 == 0)
                    break;
            }
            return resultList;
        }
        return resultList;
    }

    public static boolean compareUrl(String url1, String url2) throws MalformedURLException {
        URL urlObj1 = new URL(url1);
        URL urlObj2 = new URL(url2);
        return urlObj1.getHost().equals(urlObj2.getHost());
    }

    public static boolean checkPathBackList(String url) {
        ArrayList<String> list = new ArrayList();
        list.add("jquery");

        for (String s : list) {
            if (url.contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static String map2json(Map<String, Object> map) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        // 返回格式化后的 JSON 字符串
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
    }


    public static void sendData2Local(String data, HttpRequestResponseCallback callback) {
        SwingWorker<List<HttpRequestResponse>, Void> worker = new SwingWorker<>() {
            private List<HttpRequestResponse> httpRequestResponses = new ArrayList<>();
            private String errorMessage;

            @Override
            protected List<HttpRequestResponse> doInBackground() {
                try {
                    HttpRequest httpRequest = HttpRequest.httpRequestFromUrl("http://127.0.0.1:2334").withMethod("POST").withBody(data);
                    httpRequestResponses.add(api.http().sendRequest(httpRequest));
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


    public static Map<String,String> httpHeaderHandler(List<HttpHeader> headerList){
        Map<String,String> map = new HashMap<>();
        for (HttpHeader header : headerList) {
            map.put(header.name(),header.value());
        }
        return map;
    }

    public static Map<String,Object> covertHttpRequest2Map(HttpRequest httpRequest,Map<String,Object> paths ,Map<String,Object> params) throws MalformedURLException {
        Map<String, Object> map = new HashMap<>();
        URLParse urlParse = new URLParse(new URL(httpRequest.url()));
        map.put("url", httpRequest.url());
        map.put("method", httpRequest.method());
        map.put("headers", httpHeaderHandler(httpRequest.headers()));
        // map.put("body", httpRequest.body());
        map.put("host", urlParse.getHost());
        map.put("path", paths.values());
        map.put("protocol", urlParse.getProtocol());
        map.put("port", urlParse.getPort());
        map.put("param", params.values());
        // map.put("rootDomain", urlParse.getRootDomain());
        map.put("notes", "");
        map.put("hash",UUID.randomUUID().toString());
        // api.logging().logToOutput("map: "+map.toString());
        return map;
    }


    public static String list2BeautifulJson(List<Map<String, Object>> list) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // 返回格式化后的 JSON 字符串
        api.logging().logToOutput("objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list):"+objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list));
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
    }


    private static boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

}

