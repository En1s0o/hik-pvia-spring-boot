package io.en1s0o.hik.pvia.app.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 海康区域
 *
 * @author En1s0o
 */
@Data
public class PVIARegion {

    @JsonProperty("catalogIndexCode")
    private String catalogIndexCode;

    @JsonProperty("regionName")
    private String regionName;

    @JsonProperty("regionIndexCode")
    private String regionIndexCode;

    @JsonProperty("regionExternalCode")
    private String regionExternalCode;

    @JsonProperty("pRegionIndexCode")
    private String pRegionIndexCode;

    @JsonProperty("pRegionExternalCode")
    private String pRegionExternalCode;

    @JsonProperty("hasLeaf")
    private Integer hasLeaf;

    @JsonProperty("isPrivilege")
    private Boolean isPrivilege;

    @JsonProperty("cascadeType")
    private Integer cascadeType;

    @JsonProperty("regionType")
    private Integer regionType;

    @JsonProperty("regionLevel")
    private Integer regionLevel;

    @JsonProperty("children")
    private List<PVIARegion> children;

}
