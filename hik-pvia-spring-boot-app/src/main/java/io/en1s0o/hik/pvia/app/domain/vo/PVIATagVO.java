package io.en1s0o.hik.pvia.app.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 海康标签 VO
 *
 * @author En1s0o
 */
@Data
public class PVIATagVO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("indexCode")
    private String indexCode;

    @JsonProperty("pIndexCode")
    private String pIndexCode;

    @JsonProperty("path")
    private String path;

    @JsonProperty("hasLeaf")
    private Boolean hasLeaf;

}
