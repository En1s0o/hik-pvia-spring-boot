package io.en1s0o.hik.pvia.starter.properties;

import lombok.Data;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * HikPVIALoginProperties 海康 PVIA 登录信息配置
 *
 * @author En1s0o
 */
@Data
@ConfigurationProperties(prefix = HikPVIALoginProperties.PREFIX)
public class HikPVIALoginProperties {

    public static final String PREFIX = "hik.cas";

    /**
     * 服务器地址
     */
    private String host;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 加密算法用的密钥
     */
    private String aesKey;

    /**
     * http 链接超时时间（秒）
     */
    private Duration connectTimeout = Duration.ofSeconds(60);

    /**
     * http 读取超时时间（秒）
     */
    private Duration readTimeout = Duration.ofSeconds(60);

    /**
     * http 写入超时时间（秒）
     */
    private Duration writeTimeout = Duration.ofSeconds(60);

    /**
     * http 应用日志级别
     */
    private HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.NONE;

    /**
     * http 网络日志级别
     */
    private HttpLoggingInterceptor.Level networkLoggingLevel = HttpLoggingInterceptor.Level.NONE;

}
