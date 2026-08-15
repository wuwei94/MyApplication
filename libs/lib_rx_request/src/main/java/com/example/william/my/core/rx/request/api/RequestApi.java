package com.example.william.my.core.rx.request.api;

import com.example.william.my.core.retrofit.response.RetrofitResponse;
import com.google.gson.JsonElement;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * RxRequest 动态请求接口。
 *
 * <p>Retrofit 要求接口方法的返回类型在运行时完全确定，因此统一接收
 * {@link JsonElement}，再由上层根据请求时保存的目标类型完成反序列化。</p>
 */
public interface RequestApi {

    /**
     * 发送 GET 请求，参数以 URL 查询参数形式提交。
     *
     * @param url       完整请求地址或相对于 BaseUrl 的地址
     * @param header    请求头集合
     * @param parameter 查询参数集合
     * @return 包含统一响应结构的单次异步结果
     */
    @GET
    Single<RetrofitResponse<JsonElement>> get(@Url String url, @HeaderMap Map<String, String> header, @QueryMap Map<String, String> parameter);

    /**
     * 发送表单格式的 POST 请求。
     *
     * @param url       完整请求地址或相对于 BaseUrl 的地址
     * @param header    请求头集合
     * @param parameter 表单字段集合
     * @return 包含统一响应结构的单次异步结果
     */
    @FormUrlEncoded
    @POST
    Single<RetrofitResponse<JsonElement>> post(@Url String url, @HeaderMap Map<String, String> header, @FieldMap Map<String, String> parameter);

    /**
     * 发送带原始请求体的 POST 请求。
     *
     * <p>请求体的媒体类型由 {@code requestBody} 决定，可用于 JSON、文本等数据。</p>
     *
     * @param url         完整请求地址或相对于 BaseUrl 的地址
     * @param header      请求头集合
     * @param requestBody 原始请求体
     * @return 包含统一响应结构的单次异步结果
     */
    @POST
    Single<RetrofitResponse<JsonElement>> post(@Url String url, @HeaderMap Map<String, String> header, @Body RequestBody requestBody);

    /**
     * 发送表单格式的 PUT 请求。
     *
     * @param url       完整请求地址或相对于 BaseUrl 的地址
     * @param header    请求头集合
     * @param parameter 表单字段集合
     * @return 包含统一响应结构的单次异步结果
     */
    @FormUrlEncoded
    @PUT
    Single<RetrofitResponse<JsonElement>> put(@Url String url, @HeaderMap Map<String, String> header, @FieldMap Map<String, String> parameter);

    /**
     * 发送带原始请求体的 PUT 请求。
     *
     * @param url         完整请求地址或相对于 BaseUrl 的地址
     * @param header      请求头集合
     * @param requestBody 原始请求体，媒体类型由调用方指定
     * @return 包含统一响应结构的单次异步结果
     */
    @PUT
    Single<RetrofitResponse<JsonElement>> put(@Url String url, @HeaderMap Map<String, String> header, @Body RequestBody requestBody);

    /**
     * 发送表单格式的 PATCH 请求。
     *
     * @param url       完整请求地址或相对于 BaseUrl 的地址
     * @param header    请求头集合
     * @param parameter 表单字段集合
     * @return 包含统一响应结构的单次异步结果
     */
    @FormUrlEncoded
    @PATCH
    Single<RetrofitResponse<JsonElement>> patch(@Url String url, @HeaderMap Map<String, String> header, @FieldMap Map<String, String> parameter);

    /**
     * 发送带原始请求体的 PATCH 请求。
     *
     * @param url         完整请求地址或相对于 BaseUrl 的地址
     * @param header      请求头集合
     * @param requestBody 原始请求体，媒体类型由调用方指定
     * @return 包含统一响应结构的单次异步结果
     */
    @PATCH
    Single<RetrofitResponse<JsonElement>> patch(@Url String url, @HeaderMap Map<String, String> header, @Body RequestBody requestBody);

    /**
     * 发送 DELETE 请求，参数以 URL 查询参数形式提交。
     *
     * @param url       完整请求地址或相对于 BaseUrl 的地址
     * @param header    请求头集合
     * @param parameter 查询参数集合
     * @return 包含统一响应结构的单次异步结果
     */
    @DELETE
    Single<RetrofitResponse<JsonElement>> delete(@Url String url, @HeaderMap Map<String, String> header, @QueryMap Map<String, String> parameter);

    /**
     * 发送带原始请求体的 DELETE 请求。
     *
     * <p>{@link DELETE} 不支持请求体，因此通过 {@link HTTP} 显式启用请求体。</p>
     *
     * @param url         完整请求地址或相对于 BaseUrl 的地址
     * @param header      请求头集合
     * @param requestBody 原始请求体，媒体类型由调用方指定
     * @return 包含统一响应结构的单次异步结果
     */
    @HTTP(method = "DELETE", hasBody = true)
    Single<RetrofitResponse<JsonElement>> delete(@Url String url, @HeaderMap Map<String, String> header, @Body RequestBody requestBody);

}
