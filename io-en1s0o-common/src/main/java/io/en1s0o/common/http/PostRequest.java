package io.en1s0o.common.http;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.Getter;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * POST 请求
 *
 * @author En1s0o
 */
@SuppressWarnings("unused")
public abstract class PostRequest extends Request {

    private static final MediaType CONTENT_TYPE_PLAIN = MediaType.get("text/plain");
    private static final MediaType CONTENT_TYPE_JSON = MediaType.get("application/json");

    private PostRequest(@Nonnull String url, Map<String, String> headers) {
        super(url, headers);
    }

    /**
     * {@link #CONTENT_TYPE_PLAIN}
     */
    public static class Plain extends PostRequest {

        private final String body;

        public Plain(@Nonnull String url, Map<String, String> headers) {
            this(url, headers, null);
        }

        public Plain(@Nonnull String url, Map<String, String> headers, String body) {
            super(url, headers);
            this.body = body;
        }

        @Override
        public RequestBody getBody() {
            return RequestBody.create(CONTENT_TYPE_PLAIN, body == null ? "" : body);
        }

    }

    /**
     * {@link #CONTENT_TYPE_JSON}
     */
    public static class Json extends PostRequest {

        private static final ObjectMapper mapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        public static TreeNode fromString(String content) {
            try {
                return mapper.readTree(content);
            } catch (Exception e) {
                return JsonNodeFactory.instance.nullNode();
            }
        }

        @Getter
        private final TreeNode json;

        public Json(@Nonnull String url, Map<String, String> headers) {
            this(url, headers, null);
        }

        public Json(@Nonnull String url, Map<String, String> headers, TreeNode json) {
            super(url, headers);
            this.json = json;
        }

        @Override
        public RequestBody getBody() {
            if (json == null) {
                return RequestBody.create(CONTENT_TYPE_JSON, "");
            }

            try {
                return RequestBody.create(CONTENT_TYPE_JSON, mapper.writeValueAsString(json));
            } catch (Exception e) {
                return RequestBody.create(CONTENT_TYPE_JSON, "");
            }
        }

    }

    /**
     * Content-Type: application/x-www-form-urlencoded; charset=utf-8
     */
    public static class Form extends PostRequest {

        @Getter
        private final Map<String, String> fields;

        public Form(@Nonnull String url, Map<String, String> headers) {
            this(url, headers, new HashMap<>());
        }

        public Form(@Nonnull String url, Map<String, String> headers, @Nonnull Map<String, String> fields) {
            super(url, headers);
            this.fields = new HashMap<>(fields);
        }

        @Override
        public RequestBody getBody() {
            FormBody.Builder builder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            return builder.build();
        }

    }

    /**
     * Multipart
     */
    public static class Multipart extends PostRequest {

        @Getter
        private final Collection<MultipartBody.Part> parts;

        public Multipart(@Nonnull String url, Map<String, String> headers) {
            this(url, headers, new ArrayList<>());
        }

        public Multipart(@Nonnull String url, Map<String, String> headers, @Nonnull Collection<MultipartBody.Part> parts) {
            super(url, headers);
            this.parts = new ArrayList<>(parts);
        }

        @Override
        public RequestBody getBody() {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            for (MultipartBody.Part part : parts) {
                builder.addPart(part);
            }
            return builder.build();
        }

    }

}
