package com.codertianwei.websocket.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public final class JSONUtil {
    public static final String toJSON(Object object) {
        return JSON.toJSONString(object);
    }

    public static final JSONObject toObject(String json) {
        return JSON.parseObject(json);
    }

    public static final <T> T toObject(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }
}
