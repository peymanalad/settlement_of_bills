package com.sepehrnet.settlement.config;

import com.sepehrnet.settlement.init.StartUpInit;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;


@Setter
@Getter
@Slf4j
@ConfigurationProperties(prefix = "ftp")
public class ConfigProperties {

    private String iscUsername;
    private String iscPassword;
    private String iscIp;
    private int iscPort;

    private String gasUsername;
    private String gasPassword;
    private String gasIp;
    private int gasPort;

    private String nasimUsername;
    private String nasimPassword;
    private String nasimIp;
    private int nasimPort;

    private final StartUpInit startUpInit;

    public ConfigProperties(StartUpInit startUpInit) {
        this.startUpInit = startUpInit;
    }

    @Profile(value = "dev")
    @Bean
    public void dev() {
        log.info("FTP Connection for DEV mode.");
        ftpConnection();
    }

    @Profile(value = "prod")
    @Bean
    public void prod() {
        log.info("FTP Connection for PROD mode.");
        ftpConnection();
    }

    private void ftpConnection() {
        startUpInit.cache.put("isc.username", iscUsername);
        startUpInit.cache.put("isc.password", iscPassword);
        startUpInit.cache.put("isc.ip", iscIp);
        startUpInit.cache.put("isc.port", iscPort);

        startUpInit.cache.put("gas.username", gasUsername);
        startUpInit.cache.put("gas.password", gasPassword);
        startUpInit.cache.put("gas.ip", gasIp);
        startUpInit.cache.put("gas.port", gasPort);

        startUpInit.cache.put("nasim.username", nasimUsername);
        startUpInit.cache.put("nasim.password", nasimPassword);
        startUpInit.cache.put("nasim.ip", nasimIp);
        startUpInit.cache.put("nasim.port", nasimPort);
    }

}
