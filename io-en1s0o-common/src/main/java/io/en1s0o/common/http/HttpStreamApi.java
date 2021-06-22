package io.en1s0o.common.http;

import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

import java.util.Collection;
import java.util.Map;

/**
 * HTTP 流接口
 *
 * @author En1s0o
 */
public interface HttpStreamApi {

    /**
     * GET 请求
     *
     * @param url 请求地址
     * @return 请求对象
     */
    @GET
    @Streaming
    @Headers({"Accept: */*"})
    Call<ResponseBody> get(@Url HttpUrl url);

    /**
     * GET 请求
     *
     * @param url     请求地址
     * @param headers 额外头部
     * @return 请求对象
     */
    @GET
    @Streaming
    @Headers({"Accept: */*"})
    Call<ResponseBody> get(@Url HttpUrl url, @HeaderMap Map<String, String> headers);

    /**
     * POST 请求
     *
     * @param url     请求地址
     * @param headers 额外头部
     * @param body    请求体
     * @return 请求对象
     */
    @POST
    @Streaming
    @Headers({"Accept: */*"})
    Call<ResponseBody> post(@Url HttpUrl url, @HeaderMap Map<String, String> headers, @Body RequestBody body);

    /**
     * POST 表单请求
     *
     * @param url     请求地址
     * @param headers 额外头部
     * @param fields  表单
     * @return 请求对象
     */
    @POST
    @Streaming
    @FormUrlEncoded
    @Headers({"Accept: */*"})
    Call<ResponseBody> postForm(@Url HttpUrl url, @HeaderMap Map<String, String> headers, @FieldMap Map<String, String> fields);

    /**
     * POST Multipart 请求
     *
     * @param url     请求地址
     * @param headers 额外头部
     * @param parts   MultipartBody.Part
     * @return 请求对象
     */
    @POST
    @Streaming
    @Multipart
    @Headers({"Accept: */*"})
    Call<ResponseBody> postMultipart(@Url HttpUrl url, @HeaderMap Map<String, String> headers, @Part Collection<MultipartBody.Part> parts);

}
