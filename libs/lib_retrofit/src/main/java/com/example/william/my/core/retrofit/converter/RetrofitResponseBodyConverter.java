package com.example.william.my.core.retrofit.converter;

import com.example.william.my.core.retrofit.response.RetrofitResponse;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * 符合 RetrofitResponse 格式或直接对象格式的 ResponseBody 转换器。
 *
 * @see retrofit2.converter.gson.GsonResponseBodyConverter
 */
final class RetrofitResponseBodyConverter<T> implements Converter<ResponseBody, T> {

    private final Gson gson;
    private final TypeAdapter<T> adapter;
    private final boolean isRetrofitResponse;
    private final Type dataType;
    private final String codeKey;
    private final String msgKey;

    RetrofitResponseBodyConverter(
            Gson gson,
            TypeAdapter<T> adapter,
            boolean isRetrofitResponse,
            Type dataType,
            String codeKey,
            String msgKey
    ) {
        this.gson = gson;
        this.adapter = adapter;
        this.isRetrofitResponse = isRetrofitResponse;
        this.dataType = dataType;
        this.codeKey = codeKey != null ? codeKey : "errorCode";
        this.msgKey = msgKey != null ? msgKey : "errorMsg";
    }

    @Override
    @SuppressWarnings("unchecked")
    public T convert(ResponseBody value) throws IOException {
        String result = value.string();
        try {
            if (!isRetrofitResponse) {
                return adapter.fromJson(result);
            }

            JsonElement jsonElement = JsonParser.parseString(result);
            if (jsonElement == null || jsonElement.isJsonNull()) {
                return (T) RetrofitResponse.success(null);
            }

            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                boolean hasCustomCode = jsonObject.has(codeKey);
                boolean hasDefaultCode = jsonObject.has("errorCode");

                if (hasCustomCode || hasDefaultCode) {
                    String actualCodeKey = hasCustomCode ? codeKey : "errorCode";
                    JsonElement codeElement = jsonObject.get(actualCodeKey);
                    int code;
                    try {
                        if (codeElement == null || codeElement.isJsonNull()) {
                            throw new JsonParseException("Code field '" + actualCodeKey + "' is null");
                        }
                        code = codeElement.getAsInt();
                    } catch (Exception e) {
                        throw new JsonParseException("Failed to parse status code from field '" + actualCodeKey + "': " + codeElement, e);
                    }

                    String actualMsgKey = jsonObject.has(msgKey) ? msgKey : "errorMsg";
                    String message = "";
                    if (jsonObject.has(actualMsgKey)) {
                        JsonElement msgElement = jsonObject.get(actualMsgKey);
                        if (msgElement != null && !msgElement.isJsonNull()) {
                            message = msgElement.getAsString();
                        }
                    }

                    Object data = null;
                    if (jsonObject.has("data")) {
                        JsonElement dataElement = jsonObject.get("data");
                        if (dataElement != null && !dataElement.isJsonNull()) {
                            data = gson.fromJson(dataElement, dataType);
                        }
                    }

                    return (T) RetrofitResponse.of(code, message, data);
                }
            }

            Object data = gson.fromJson(jsonElement, dataType);
            return (T) RetrofitResponse.success(data);
        } finally {
            value.close();
        }
    }
}