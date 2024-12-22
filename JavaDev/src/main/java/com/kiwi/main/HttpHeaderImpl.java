package com.kiwi.main;

import burp.api.montoya.http.message.HttpHeader;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaderImpl implements HttpHeader {

    @Override
    public String name() {
        return "";
    }

    @Override
    public String value() {
        return "";
    }

    @Override
    public String toString() {
        Map<String, String> map = new HashMap<>();

        return super.toString();
    }
}
