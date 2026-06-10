package com.hitanalysis.common.utils;

import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;

/**
 * JSON utility class based on Hutool
 */
@Component
public class JsonUtils {

    /**
     * Object to JSON string
     */
    public String toJson(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }

    /**
     * JSON string to object
     */
    public <T> T toObject(String json, Class<T> clazz) {
        return JSONUtil.toBean(json, clazz);
    }

    /**
     * JSON string to pretty format
     */
    public String toPrettyJson(Object obj) {
        return JSONUtil.toJsonPrettyStr(obj);
    }

    /**
     * Check if string is valid JSON
     */
    public boolean isValidJson(String str) {
        return JSONUtil.isJsonArray(str) || JSONUtil.isJsonObj(str);
    }
}