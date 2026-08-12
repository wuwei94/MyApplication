package com.example.william.my.core.retrofit.rx.dynamic;

import com.example.william.my.core.retrofit.response.RetrofitResponse;
import com.google.gson.JsonElement;

import java.util.Map;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * RxDynamicRequest 动态请求接口。
 *
 * <p>Retrofit 要求接口方法的返回类型在运行时完全确定，因此统一接收
 * {@link JsonElement}，再由上层根据请求时保存的目标类型完成反序列化。</p>
 */
interface RxDynamicRequestApi {

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

    /**
     * 以 {@code multipart/form-data} 格式上传单个文件分段。
     *
     * @param url    完整请求地址或相对于 BaseUrl 的地址
     * @param header 请求头集合
     * @param part   已包含表单字段名、文件名和文件内容的文件分段
     * @return 包含统一响应结构的单次异步结果
     */
    @Multipart
    @POST
    Single<RetrofitResponse<JsonElement>> uploadFile(@Url String url, @HeaderMap Map<String, String> header, @Part MultipartBody.Part part);

    /**
     * 以 {@code multipart/form-data} 格式上传多个请求体分段。
     *
     * @param url    完整请求地址或相对于 BaseUrl 的地址
     * @param header 请求头集合
     * @param map    表单字段名与请求体的映射
     * @return 包含统一响应结构的单次异步结果
     */
    @Multipart
    @POST
    Single<RetrofitResponse<JsonElement>> uploadFiles(@Url String url, @HeaderMap Map<String, String> header, @PartMap Map<String, RequestBody> map);

    /**
     * 以流式方式下载文件，并通过 Range 请求头支持断点续传。
     *
     * <p>{@link Streaming} 避免 Retrofit 将响应体一次性读入内存，调用方负责消费并关闭响应体。</p>
     *
     * @param range 字节范围，例如 {@code bytes=1024-}
     * @param url   完整下载地址或相对于 BaseUrl 的地址
     * @return 文件响应体的单次异步结果
     */
    @Streaming
    @GET
    Single<ResponseBody> downloadFile(@Header("RANGE") String range, @Url String url);
}
