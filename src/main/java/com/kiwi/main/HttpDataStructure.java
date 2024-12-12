package com.kiwi.main;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class HttpDataStructure implements Serializable {
    private List<Map<URLParse, List<Map<String, String>>>> data;

    public HttpDataStructure() {
        this.data = new ArrayList<>();
    }

    // 添加一个元素并检查重复性
    public boolean addElement(Map<URLParse, List<Map<String, String>>> newElement) {
        Set<String> existingKeys = collectAllKeys();
        // 检查新元素中的Key是否重复
        for (URLParse urlParse : newElement.keySet()) {
            if (existingKeys.contains(urlParse.getUrl())) {
                return false; // 不添加
            }
        }
        // 如果没有重复Key，添加到数据中
        data.add(newElement);
        return true;
    }

    // 收集所有的 Key
    private Set<String> collectAllKeys() {
        Set<String> keys = new HashSet<>();
        for (Map<URLParse, List<Map<String, String>>> map : data) {
            // 获取当前 map 的键集
            Set<URLParse> urlParseSet = map.keySet();
            // 遍历键集
            for (URLParse urlParse : urlParseSet) {
                // 将键的字符串表示添加到 keys 集合中
                keys.add(urlParse.getUrl());
            }
        }
        return keys;
    }

    // 获取所有数据
    public List<Map<URLParse, List<Map<String, String>>>> getData() {
        return data;
    }

    // 添加一个Map类型的数据到data
    public void setData(Map<URLParse, List<Map<String, String>>> newElement) {
        // 将newElement添加到List
        data.add(newElement);
    }

    public List<Map<String, String>> getParamFromUrl(String url) throws MalformedURLException {
        // 依据url从data当中取出相对于的数据
        URLParse urlParse = new URLParse(new URL(url));
        List<Map<String, String>> params = new ArrayList<>();
        for (Map<URLParse, List<Map<String, String>>> listMap : data ) {
            for (Map.Entry<URLParse, List<Map<String, String>>> entry : listMap.entrySet()) {
                if (entry.getKey().getRootDomain().equals(urlParse.getRootDomain())) {
                    params.addAll(entry.getValue());
                }
            }
        }
        return params;
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
