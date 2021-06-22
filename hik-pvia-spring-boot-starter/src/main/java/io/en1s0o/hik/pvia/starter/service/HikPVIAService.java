package io.en1s0o.hik.pvia.starter.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import io.en1s0o.common.cookie.InMemoryCookieJar;
import io.en1s0o.hik.pvia.starter.properties.HikPVIALoginProperties;
import io.en1s0o.common.http.GetRequest;
import io.en1s0o.common.http.HttpApi;
import io.en1s0o.common.http.PostRequest;
import io.en1s0o.common.http.Request;
import io.en1s0o.common.security.AESSecurity;
import io.en1s0o.common.ssl.SSLHelper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.bouncycastle.util.encoders.Base64;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;

/**
 * 海康 PVIA 服务
 *
 * @author En1s0o
 */
@Slf4j
public class HikPVIAService {

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final String PORTAL = "/portal/";
    private static final String X_AUTH_WEB = "/xauth-web/";
    private static final String X_RESMGR_WEB = "/xresmgr-web/";
    private static final String MCS = "/mcs/";
    private static final String X_CONFIG_WEB = "/xconfig-web/";
    private static final String NMS = "/nms/";

    private ListeningExecutorService workExecutor;

    // 在成功登录后，第一次应该访问一些简单的 GET 请求，完成真正登录
    private final Map<String, Boolean> simpleGetRequestMap;
    private final Map<String, String> simpleGetUrlMap;

    // 有些接口需要跨域验证
    private final Map<String, String> csrfMap;
    private final Map<String, String> csrfUrlMap;

    private final byte[] aesKey;
    private final HttpUrl host;
    private final HttpApi api;

    public HikPVIAService(HikPVIALoginProperties props) throws GeneralSecurityException {
        simpleGetRequestMap = new HashMap<>();
        simpleGetUrlMap = new HashMap<>();
        csrfMap = new HashMap<>();
        csrfUrlMap = new HashMap<>();
        aesKey = props.getAesKey().getBytes(StandardCharsets.UTF_8);
        host = HttpUrl.get(props.getHost());

        simpleGetUrlMap.put(PORTAL, "/portal/tool/ajax/queryTool.do?type=1");
        simpleGetUrlMap.put(X_AUTH_WEB, "/xauth-web/dept/getAsyncTreeNodes.do");
        simpleGetUrlMap.put(X_CONFIG_WEB, "/xconfig-web/getWatermarkConfig.do");
        simpleGetUrlMap.put(MCS, "/mcs/web/recordSchedule/checkProgress.do");
        simpleGetUrlMap.put(X_RESMGR_WEB, "/xresmgr-web/view/overview?menuId=xresmgr_0500");
        simpleGetUrlMap.put(NMS, "/nms/ui/v1/alarmlevel/index");

        csrfUrlMap.put(X_AUTH_WEB, "/xauth-web/user/index.html");
        csrfUrlMap.put(X_CONFIG_WEB, "/xconfig-web/index.do");
        csrfUrlMap.put(X_RESMGR_WEB, "/xresmgr-web/view/overview?menuId=xresmgr_0500");

        SSLHelper sslHelper = new SSLHelper(true);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(props.getConnectTimeout())
                .readTimeout(props.getReadTimeout())
                .writeTimeout(props.getWriteTimeout())
                .cookieJar(new InMemoryCookieJar())
                .addInterceptor(new HttpLoggingInterceptor(log::info)
                        .setLevel(props.getLoggingLevel()))
                .addInterceptor(new HikCasLoginInterceptor(props.getUsername(), props.getPassword(), () -> {
                    log.warn("evict all");
                    simpleGetRequestMap.clear();
                    csrfMap.clear();
                }))
                .addNetworkInterceptor(new HttpLoggingInterceptor(log::info)
                        .setLevel(props.getNetworkLoggingLevel()))
                .sslSocketFactory(sslHelper.getSSLSocketFactory(), sslHelper.getX509TrustManager())
                .hostnameVerifier(sslHelper.getHostnameVerifier())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(props.getHost())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(okHttpClient)
                .build();

        api = retrofit.create(HttpApi.class);
    }

    @PostConstruct
    public void init() {
        workExecutor = MoreExecutors.listeningDecorator(new ForkJoinPool(64, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true));
    }

    @PreDestroy
    public void destroy() {
        if (workExecutor != null) {
            workExecutor.shutdownNow();
        }
    }

    private ListenableFuture<ResponseBody> process(Call<ResponseBody> call) {
        return workExecutor.submit(() -> {
            Response<ResponseBody> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }

            ResponseBody body = response.body();
            if (body != null) {
                body.close();
            }

            return response.errorBody();
        });
    }

    /**
     * 根据前缀获取 csrf
     *
     * @param key 前缀
     * @return csrf
     */
    private String fetchCSRF(String key) {
        // 经过调试页面，得到 csrf 的页面地址，记录在 csrfUrlMap 中
        String url = csrfUrlMap.get(key);
        if (url == null) {
            return null;
        }

        Request request = new GetRequest(url);
        try (ResponseBody body = process(api.get(request.resolveHttpUrl(host))).get()) {
            // 解析页面，并得到 <head> 标签下的所有 <meta> 标签
            Document doc = Jsoup.parse(body.string());
            Elements metas = doc.head().getElementsByTag("meta");
            // 遍历 <meta> 标签，找到 name=_csrf
            // <meta name=_csrf content=d9d71ee5-70a7-4b0b-b17e-8a8155345ed8>
            for (Element meta : metas) {
                if (meta.hasAttr("name") && meta.hasAttr("content")) {
                    if ("_csrf".equals(meta.attr("name"))) {
                        return meta.attr("content");
                    }
                }
            }
        } catch (Exception e) {
            log.error(String.format("[%s] fetch CSRF token failed", key), e);
        }
        return null;
    }

    /**
     * 根据 url 获取 csrf
     *
     * @param url 将要请求的 url
     * @return csrf
     */
    private String getCSRFByUrl(HttpUrl url) {
        String path = url.encodedPath();
        String[] keys = new String[]{X_AUTH_WEB, X_RESMGR_WEB, X_CONFIG_WEB};
        for (String key : keys) {
            if (path.startsWith(key)) {
                return csrfMap.computeIfAbsent(key, this::fetchCSRF);
            }
        }
        return null;
    }

    /**
     * 初始化请求
     *
     * @param request GET 请求
     * @return 成功 true，否则 null
     */
    private Boolean initRequest(GetRequest request) {
        try {
            HttpUrl url = request.resolveHttpUrl(host);
            Call<ResponseBody> call = api.get(url, request.getHeaders());
            ResponseBody body = process(call).get();
            if (body != null) {
                body.close();
                return true;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private void ensureFirstGetRequest(HttpUrl url, Map<String, String> headers) {
        String path = url.encodedPath();
        String[] keys = new String[]{PORTAL, X_AUTH_WEB, X_RESMGR_WEB, MCS, X_CONFIG_WEB, NMS};
        for (String key : keys) {
            if (path.startsWith(key)) {
                simpleGetRequestMap.computeIfAbsent(key,
                        k -> initRequest(new GetRequest(simpleGetUrlMap.get(k), headers)));
                return;
            }
        }
    }

    private byte[] doRequestInternal(Request request) throws Exception {
        HttpUrl url = request.resolveHttpUrl(host);
        Map<String, String> headers = new HashMap<>(request.getHeaders());

        // 获取 csrf，有的页面可能不存在
        String csrf = getCSRFByUrl(url);
        if (csrf != null) {
            headers.put("X-CSRF-TOKEN", csrf);
        }
        // 确保发出第一个 GET 请求，如果没有这个请求，当第一个请求为 POST 请求，那么总是失败
        ensureFirstGetRequest(url, headers);

        Call<ResponseBody> call;
        if (request instanceof GetRequest) {
            call = api.get(url, headers);
        } else {
            if (request instanceof PostRequest.Form) {
                call = api.postForm(url, headers, ((PostRequest.Form) request).getFields());
            } else if (request instanceof PostRequest.Multipart) {
                Collection<MultipartBody.Part> parts = new ArrayList<>();
                if (csrf != null) {
                    parts.add(MultipartBody.Part.createFormData("_csrf", csrf));
                }
                parts.addAll(((PostRequest.Multipart) request).getParts());
                call = api.postMultipart(url, headers, parts);
            } else {
                // Plain, Json
                call = api.post(url, headers, request.getBody());
            }
        }

        byte[] response;
        try (ResponseBody body = process(call).get()) {
            response = body.bytes();
        }
        try {
            // 目前观测到的错误，符合：type=-3, code="403"
            // {
            //   "type": -3,
            //   "code": "403",
            //   "msg": "Could not verify the provided CSRF token because your session was not found.",
            //   "data": {
            //     "referer": null,
            //     "appUrl": "/xconfig-web/index.do",
            //     "requestURI": "/xconfig-web/refreshCustomProps.do"
            //   }
            // }
            Map<?, ?> map = mapper.readValue(response, Map.class);
            if (map != null && Objects.equals(-3, map.get("type")) && "403".equals(map.get("code"))) {
                // evict all
                simpleGetRequestMap.clear();
                csrfMap.clear();
            }
        } catch (Exception ignored) {
        }
        return response;
    }

    /**
     * 发出请求
     *
     * @param request 请求
     * @return 结果
     * @throws Exception 发生异常
     */
    public <T> io.en1s0o.common.http.Response<T> doRequest(Request request, Class<T> clazz) throws Exception {
        byte[] response = doRequestInternal(request);
        if (simpleGetRequestMap.isEmpty() || csrfMap.isEmpty()) {
            response = doRequestInternal(request);
        }
        return io.en1s0o.common.http.Response.create(response, clazz);
    }

    /**
     * 用于加密设备密码
     *
     * @param content 设备明文密码
     * @return 密文 + Base64 密码
     */
    public String encrypt(String content) {
        try {
            byte[] data = content.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedData = AESSecurity.encrypt(data, aesKey);
            return Base64.toBase64String(encryptedData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * 用于解密设备密码
     *
     * @param content 密文 + Base64 密码
     * @return 明文密码
     */
    public String decrypt(String content) {
        try {
            byte[] data = Base64.decode(content);
            byte[] decryptedData = AESSecurity.decrypt(data, aesKey);
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

}
