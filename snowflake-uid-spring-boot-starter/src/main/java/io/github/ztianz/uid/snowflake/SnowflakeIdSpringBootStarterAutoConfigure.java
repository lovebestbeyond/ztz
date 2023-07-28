package io.github.ztianz.uid.snowflake;

import io.github.ztianz.uid.snowflake.core.service.SnowflakeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ztz
 * @date 2023/02/01
 */
@Configuration
@EnableConfigurationProperties(SnowflakeIdSpringBootProperties.class)
@Slf4j
public class SnowflakeIdSpringBootStarterAutoConfigure {

    @Autowired
    private SnowflakeIdSpringBootProperties properties;

    @Bean
    @ConditionalOnMissingBean(SnowflakeService.class)
    public SnowflakeService initSnowflakeService() throws Exception {
        if (properties != null) {
            SnowflakeService snowflakeService = new SnowflakeService(
                    properties.getDriverClassName(),
                    properties.getUrl(),
                    properties.getUsername(),
                    properties.getPassword(),
                    properties.getApplicationName(),
                    properties.getServerPort(),
                    properties.getWorkerIdBits(),
                    properties.getSequenceBits(),
                    properties.getTwepoch(),
                    properties.getInterfaceName(),
                    properties.getHeartBeat());
            return snowflakeService;
        }
        log.warn("init ztz-id SnowflakeService ignore properties is {}", properties);
        return null;
    }
}
