package io.en1s0o.hik.pvia.starter.config;

import io.en1s0o.hik.pvia.starter.properties.HikPVIALoginProperties;
import io.en1s0o.hik.pvia.starter.properties.HikPVIAProperties;
import io.en1s0o.hik.pvia.starter.service.HikPVIAService;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;

/**
 * HikPVIAConfiguration
 *
 * @author En1s0o
 */
@Data
@Configuration
@EnableConfigurationProperties({HikPVIAProperties.class, HikPVIALoginProperties.class})
public class HikPVIAAutoConfiguration {

    private HikPVIAProperties properties;

    private HikPVIALoginProperties loginProperties;

    public HikPVIAAutoConfiguration(
            HikPVIAProperties properties,
            HikPVIALoginProperties loginProperties) {
        this.properties = properties;
        this.loginProperties = loginProperties;
    }

    @Bean
    public HikPVIAService hikPVIAService() throws GeneralSecurityException {
        return new HikPVIAService(loginProperties);
    }

}
