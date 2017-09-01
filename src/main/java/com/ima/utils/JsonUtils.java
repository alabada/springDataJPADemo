package com.ima.utils;

import com.alibaba.fastjson.JSONArray;

public class JsonUtils {

    public static String objectToJson(Object data) {
        String json = JSONArray.toJSONString(data);
        return json;
    }
}
