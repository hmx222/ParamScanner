package com.kiwi.main;

import java.net.URL;

public class URLParse {
    URL urlParse;

    public URLParse(URL urlParse) {

        this.urlParse = urlParse;
    }
    public String getUrl() {
        return urlParse.toString();
    }

    public int getPort() {
        return urlParse.getPort();
    }

    public String getHost() {
        return urlParse.getHost();
    }

    public String getPath() {
        return urlParse.getPath();
    }

    public String getProtocol() {
        return urlParse.getProtocol();
    }

    public String getQuery() {
        return urlParse.getQuery();
    }

    public String getRootDomain() {
        String host = urlParse.getHost();
        String[] parts = host.split("\\.");
        if (parts.length > 2) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        } else {
            return host;
        }
    }

    public String getUrlWithoutQuery() {
        return urlParse.toString().replace("?" + urlParse.getQuery(), "");
    }

    public String getUrlWithoutPath() {
        return urlParse.toString().replace(urlParse.getPath(), "");
    }
}
