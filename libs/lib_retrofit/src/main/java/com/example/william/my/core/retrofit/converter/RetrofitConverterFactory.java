package com.example.william.my.core.retrofit.converter;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 重写父类中responseBodyConverter，该方法用来转换服务器返回数据
 * 重写父类中requestBodyConverter，该方法用来转换发送给服务器的数据
 *
 * @see retrofit2.converter.gson.GsonConverterFactory
 */
public class RetrofitConverterFactory extends Converter.Factory {

    /**
     * 使用默认 {@link Gson} 实例创建转换器。
     *
     * @deprecated 使用 {@link #create(String, String)} 直接指定 code/message 字段名。
     */
    @Deprecated
    public static RetrofitConverterFactory create() {
        return create(new Gson(), "errorCode", "errorMsg");
    }

    /**
     * 使用指定 {@code gson} 实例创建转换器。
     *
     * @deprecated 使用 {@link #create(Gson, String, String)} 直接指定 code/message 字段名。
     */
    @Deprecated
    public static RetrofitConverterFactory create(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new RetrofitConverterFactory(gson, "errorCode", "errorMsg");
    }

    public static RetrofitConverterFactory create(String code, String message) {
        return create(new Gson(), code, message);
    }

    public static RetrofitConverterFactory create(Gson gson, String code, String message) {
        if (gson == null) throw new NullPointerException("gson == null");
        return new RetrofitConverterFactory(gson, code, message);
    }

    private final Gson gson;
    private final String code, message;

    private RetrofitConverterFactory(Gson gson, String code, String message) {
        this.gson = gson;
        this.code = code;
        this.message = message;
    }

    /**
     * 需要重写父类中responseBodyConverter，该方法用来转换服务器返回数据
     */
    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(@NonNull Type type, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new RetrofitResponseBodyConverter<>(gson, adapter, code, message);
    }

    /**
     * 需要重写父类中requestBodyConverter，该方法用来转换发送给服务器的数据
     */
    @Override
    public Converter<?, RequestBody> requestBodyConverter(@NonNull Type type, @NonNull Annotation[] parameterAnnotations, @NonNull Annotation[] methodAnnotations, @NonNull Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new RetrofitRequestBodyConverter<>(gson, adapter);
    }
}
