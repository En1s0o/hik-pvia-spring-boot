package io.en1s0o.hik.pvia.starter.domain.vo;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PVIA 分页 VO
 *
 * @param <T> 数据类型
 * @author En1s0o
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class PVIAPageVO<T> extends PVIABaseVO<PVIAPageVO.PVIAPage<T>> {

    @Data
    @JsonSerialize(using = PVIAPage.Serializer.class)
    public static class PVIAPage<T> {

        @JsonProperty("pageNo")
        private Long pageNo;

        @JsonProperty("pageSize")
        private Long pageSize;

        @JsonProperty("totalPage")
        private Long totalPage;

        @JsonProperty("total")
        private Long total;

        @JsonProperty("list")
        private List<T> list;

        @JsonAnySetter
        @JsonProperty("_")
        private Map<String, Object> unknown = new HashMap<>();

        public static final class Serializer extends JsonSerializer<PVIAPage<?>> {

            @Override
            public void serialize(PVIAPage value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeStartObject();
                gen.writeObjectField("pageNo", value.getPageNo());
                gen.writeObjectField("pageSize", value.getPageSize());
                gen.writeObjectField("totalPage", value.getTotalPage());
                gen.writeObjectField("total", value.getTotal());
                gen.writeObjectField("list", value.getList());
                if (!value.getUnknown().isEmpty()) {
                    gen.writeObjectField("_", value.getUnknown());
                }
                gen.writeEndObject();
            }

        }

    }

}
