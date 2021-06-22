package io.en1s0o.hik.pvia.starter.domain.vo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * PVIA 基础 VO
 *
 * @param <T> 数据类型
 * @author En1s0o
 */
@Data
@JsonSerialize(using = PVIABaseVO.Serializer.class)
public abstract class PVIABaseVO<T> {

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("code")
    private String code;

    @JsonAlias("msg")
    @JsonProperty("message")
    private String message;

    @JsonProperty("error")
    private String error;

    @JsonProperty("data")
    private T data;

    @JsonAnySetter
    @JsonProperty("_")
    private Map<String, Object> unknown = new HashMap<>();

    public static final class Serializer extends JsonSerializer<PVIABaseVO<?>> {

        @Override
        public void serialize(PVIABaseVO value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("type", value.getType());
            gen.writeObjectField("code", value.getCode());
            gen.writeObjectField("message", value.getMessage());
            gen.writeObjectField("error", value.getError());
            gen.writeObjectField("data", value.getData());
            if (!value.getUnknown().isEmpty()) {
                gen.writeObjectField("_", value.getUnknown());
            }
            gen.writeEndObject();
        }

    }

}
