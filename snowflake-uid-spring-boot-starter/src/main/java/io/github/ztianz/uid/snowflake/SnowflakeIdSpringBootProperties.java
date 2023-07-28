package io.github.ztianz.uid.snowflake;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ztz
 * @date 2023/02/01
 */
@ConfigurationProperties(prefix = "ztz-uid.snowflake", ignoreUnknownFields = true)
public class SnowflakeIdSpringBootProperties {
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private String applicationName;
    private Integer serverPort;
    private Long workerIdBits;
    private Long sequenceBits;
    private Long twepoch;
    private String interfaceName;
    private Boolean heartBeat;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public Long getWorkerIdBits() {
        return workerIdBits;
    }

    public void setWorkerIdBits(Long workerIdBits) {
        this.workerIdBits = workerIdBits;
    }

    public Long getSequenceBits() {
        return sequenceBits;
    }

    public void setSequenceBits(Long sequenceBits) {
        this.sequenceBits = sequenceBits;
    }

    public Long getTwepoch() {
        return twepoch;
    }

    public void setTwepoch(Long twepoch) {
        this.twepoch = twepoch;
    }

    public Boolean getHeartBeat() {
        return heartBeat;
    }

    public void setHeartBeat(Boolean heartBeat) {
        this.heartBeat = heartBeat;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
}
