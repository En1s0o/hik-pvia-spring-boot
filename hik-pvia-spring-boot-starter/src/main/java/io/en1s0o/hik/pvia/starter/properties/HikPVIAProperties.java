package io.en1s0o.hik.pvia.starter.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HikPVIAProperties
 *
 * @author En1s0o
 */
@Data
@ConfigurationProperties(prefix = HikPVIAProperties.PREFIX)
public class HikPVIAProperties {

    public static final String PREFIX = "hik.organization";

    /**
     * 目录区域码，例如："10"
     */
    private String catalogIndexCode;

    /**
     * 区域名称，例如："仲恺视频汇聚"
     */
    private String regionName;

    /**
     * 区域索引码（区域内码），例如："0390d72fb6574329a654e1c15aea5819"
     */
    private String regionIndexCode;

    /**
     * 区域外码，例如："44130000002160000003"
     */
    private String regionExternalCode;

}
