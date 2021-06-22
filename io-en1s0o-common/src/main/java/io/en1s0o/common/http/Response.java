package io.en1s0o.common.http;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;

import java.io.DataInput;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;

/**
 * 基本回复
 *
 * @author En1s0o
 */
@Slf4j
public abstract class Response<T> {

    private static final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private final Type type;
    private final TypeReference<T> valueTypeRef;
    private final Object raw;
    private boolean parsed;
    private T parsedRes;

    public static <T> Response<T> create(Object raw, Class<T> clazz) {
        return new Response<T>(raw, clazz) {
        };
    }

    private Response(Object raw, Class<T> clazz) {
        this.raw = raw;
        if (clazz == null) {
            Type superClass = getClass().getGenericSuperclass();
            Type t = ((ParameterizedType) superClass).getActualTypeArguments()[0];
            this.type = t instanceof Class<?> ? t : Object.class;
        } else {
            this.type = clazz;
        }
        this.valueTypeRef = new TypeReference<T>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }

    @SuppressWarnings("unchecked")
    private synchronized void parse() throws Exception {
        if (parsed) {
            return;
        }
        parsed = true;
        parsedRes = null;

        if (raw == null) {
            return;
        }

        // T = String | byte[]
        if (String.class.isAssignableFrom((Class<?>) type) ||
                byte[].class.isAssignableFrom((Class<?>) type)) {
            if (raw instanceof String) {
                try {
                    parsedRes = (T) mapper.writeValueAsString(mapper.readTree((String) raw));
                } catch (Exception e) {
                    parsedRes = (T) raw;
                }
                return;
            }
            if (raw instanceof byte[]) {
                try {
                    parsedRes = (T) mapper.writeValueAsString(mapper.readTree((byte[]) raw));
                } catch (Exception e) {
                    parsedRes = (T) raw;
                }
                return;
            }
        }

        JsonNode json;
        if (raw instanceof byte[]) {
            json = mapper.readTree((byte[]) raw);
        } else if (raw instanceof String) {
            json = mapper.readTree((String) raw);
        } else if (raw instanceof JsonParser) {
            json = mapper.readTree((JsonParser) raw);
        } else if (raw instanceof InputStream) {
            json = mapper.readTree((InputStream) raw);
        } else if (raw instanceof Reader) {
            json = mapper.readTree((Reader) raw);
        } else if (raw instanceof File) {
            json = mapper.readTree((File) raw);
        } else if (raw instanceof URL) {
            json = mapper.readTree((URL) raw);
        } else if (raw instanceof DataInput) {
            json = mapper.readValue((DataInput) raw, JsonNode.class);
        } else if (raw instanceof ResponseBody) {
            try (ResponseBody body = (ResponseBody) raw) {
                json = mapper.readTree(body.bytes());
            }
        } else {
            json = mapper.convertValue(raw, JsonNode.class);
        }

        // T = Object
        if (((Class<?>) type).isAssignableFrom(Object.class)) {
            parsedRes = (T) json;
        } else if (String.class.isAssignableFrom((Class<?>) type)) {
            parsedRes = (T) mapper.writeValueAsString(json);
        } else {
            parsedRes = mapper.convertValue(json, valueTypeRef);
        }
    }

    @SuppressWarnings("unchecked")
    public T get() {
        try {
            parse();
        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        if (parsedRes == null) {
            if (byte[].class.isAssignableFrom((Class<?>) type)) {
                if (raw instanceof byte[]) {
                    return (T) raw;
                }
            }
        }

        return parsedRes;
    }

}
