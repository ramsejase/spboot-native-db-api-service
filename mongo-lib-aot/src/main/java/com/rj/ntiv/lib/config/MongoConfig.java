package com.rj.ntiv.lib.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mongo")
public class MongoConfig {
    List<DatabaseProperties> config;

    public void setConfig(List<DatabaseProperties> config) {
        this.config = config;
    }

    public List<DatabaseProperties> getConfig() {
        return config;
    }
}