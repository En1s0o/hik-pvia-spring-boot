package io.en1s0o.hik.pvia.starter.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.annotations.EverythingIsNonNull;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * HikCasLoginInterceptor
 *
 * @author En1s0o
 */
@Slf4j
@EverythingIsNonNull
class HikCasLoginInterceptor implements Interceptor {

    private static final String CAS_LOGIN = "/portal/cas/loginPage";

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Runnable evict;
    private final String username;
    private final String password;

    HikCasLoginInterceptor(String username, String password, Runnable evict) {
        this.evict = evict;
        this.username = username;
        this.password = ByteUtils.toHexString(new SHA256.Digest().digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    private String encryptPassword(String vCode) {
        byte[] data = (password + vCode).getBytes(StandardCharsets.UTF_8);
        return ByteUtils.toHexString(new SHA256.Digest().digest(data));
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        // 失败时直接返回。非登录页面也直接返回
        if (!response.isSuccessful() || !isLoginPage(response)) {
            return response;
        }

        evict.run();
        // 如果登录页面异常，直接返回即可
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return response;
        }
        String content = responseBody.string();

        HttpUrl loginPageUrl = response.request().url();
        Response loginResponse = login(chain, loginPageUrl);
        if (loginResponse == null) {
            return response.newBuilder()
                    .body(ResponseBody.create(responseBody.contentType(), content))
                    .build();
        }
        if (!loginResponse.isSuccessful() || maybe(request, loginResponse.request())) {
            // 失败时直接返回。如果成功了，并且是原始请求，那么直接返回即可
            return loginResponse;
        }
        loginResponse.close();

        return tryRequest(chain, request);
    }

    private Response tryRequest(Chain chain, Request request) throws IOException {
        // 这里最多重试 3 次，因为是单点登录，所以不能保证重试 1 次就会成功
        // GET 接口一般需要 1 次；POST 接口一般需要 2 次
        // 暂时没有遇到需要 3 次的情况
        for (int i = 0; i < 2; i++) {
            Response response = chain.proceed(request);
            if (maybe(request, response.request())) {
                return response;
            } else {
                response.close();
            }
        }

        return chain.proceed(request);
    }

    private boolean maybe(Request ra, Request rb) {
        if (ra == rb) {
            return true;
        }
        // 不是 GET 请求，由于 BODY 的不确定性，所以都认为是不同的
        return ra.method().equals("GET") && ra.method().equals(rb.method()) && ra.url().equals(rb.url());
    }

    @Nullable
    private Response login(Chain chain, HttpUrl loginPageUrl) throws IOException {
        HttpUrl submitUrl = loginPageUrl.resolve("/portal/login/ajax/submit.do");
        if (submitUrl == null) {
            return null;
        }

        // 获取登录表单
        Response loginDataResponse = fetchLoginData(chain, loginPageUrl);
        if (loginDataResponse == null || !loginDataResponse.isSuccessful()) {
            return loginDataResponse;
        }
        ResponseBody loginDataBody = loginDataResponse.body();
        if (loginDataBody == null) {
            return loginDataResponse;
        }

        // 构建登录表单
        String loginData = loginDataBody.string();
        RequestBody loginRequestBody = makeLoginBody(loginPageUrl, loginData);
        if (loginRequestBody == null) {
            // 构建登录表单失败，无法进行后续的请求
            return loginDataResponse.newBuilder()
                    .body(ResponseBody.create(loginDataBody.contentType(), loginData))
                    .build();
        }

        // 登录
        Request loginRequest = new Request.Builder()
                .url(submitUrl)
                .method("POST", loginRequestBody)
                .build();
        Response loginResponse = chain.proceed(loginRequest);
        ResponseBody loginResponseBody = loginResponse.body();
        if (!loginResponse.isSuccessful() || loginResponseBody == null) {
            // 登录失败，无法进行后续的请求
            return loginResponse;
        }

        // 单点登录
        String ssoLoginData = loginResponseBody.string();
        HttpUrl ssoLoginUrl = getSsoLoginUrl(ssoLoginData);
        if (ssoLoginUrl == null) {
            // 单点登录失败，无法进行后续的请求
            return loginResponse.newBuilder()
                    .body(ResponseBody.create(loginResponseBody.contentType(), ssoLoginData))
                    .build();
        }

        return chain.proceed(new Request.Builder().url(ssoLoginUrl).build());
    }

    private boolean isLoginPage(Response response) {
        // 如果 response 是登录页面，必须满足 GET 请求，并且以 /portal/cas/loginPage 开始
        Request request = response.request();
        HttpUrl url = request.url();
        return "GET".equals(request.method()) && url.encodedPath().startsWith(CAS_LOGIN);
    }

    private boolean isSuccessful(@Nullable JsonNode json) {
        return json != null && json.hasNonNull("success") && json.get("success").asBoolean();
    }

    @Nullable
    private Response fetchLoginData(Chain chain, HttpUrl loginPageUrl) throws IOException {
        // [ref] POST /portal/login/ajax/postLoginData.do
        // 如果用户名不存在，这个结果会返回错误信息
        // {"success":false,"errorCode":"0x06b01006","errorMsg":"用户名或密码错误"}
        HttpUrl url = loginPageUrl.resolve("/portal/login/ajax/postLoginData.do");
        if (url != null) {
            RequestBody body = RequestBody.create(
                    MediaType.parse("application/x-www-form-urlencoded"),
                    "userName=" + username);
            Request request = new Request.Builder()
                    .url(url)
                    .method("POST", body)
                    .build();
            return chain.proceed(request);
        }
        return null;
    }

    @Nullable
    private RequestBody makeLoginBody(HttpUrl loginPageUrl, String content) {
        try {
            ObjectNode json = mapper.readValue(content, ObjectNode.class);
            if (isSuccessful(json) && json.hasNonNull("data")) {
                JsonNode data = json.get("data");
                if (data.hasNonNull("vCode")) {
                    // [ref] POST /portal/login/ajax/submit.do
                    String password = encryptPassword(data.get("vCode").asText());
                    String serviceUrl = URLEncoder.encode(loginPageUrl.toString(), "UTF-8");
                    String codeId = data.get("codeId").asText("");
                    String loginForm = "userName=" + username + "&" +
                            "password=" + password + "&" +
                            "serviceUrl=" + serviceUrl + "&" +
                            "codeId=" + codeId + "&" +
                            "imageCode=&userType=0&lang=zh_CN";
                    return RequestBody.create(
                            MediaType.parse("application/x-www-form-urlencoded"),
                            loginForm);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    @Nullable
    private HttpUrl getSsoLoginUrl(String content) {
        // {"success":true,"data":"https://xxx:443/bic/ssoService/v1/tokenLogin?token=ST-9908-eg3b0f1CKgpmBZOacixS-cas&service=https://10.228.213.196:443/portal/login?redirect=aHR0cHM6Ly8xMC4yMjguMjEzLjE5Njo0NDMvcG9ydGFs"}
        try {
            ObjectNode json = mapper.readValue(content, ObjectNode.class);
            if (isSuccessful(json) && json.hasNonNull("data")) {
                return HttpUrl.get(json.get("data").asText());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
