package com.yo1000.s3import.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "app.time")
public class TimeProperties {
    private String updateCron = "0 0 */12 * * *";
    private long waitUnitTimeMillis = Duration.ofMinutes(1).toMillis();
    private long minUpdateIntervalMillis = Duration.ofMinutes(30).toMillis();
    private long nodeKeepAliveMillis = Duration.ofDays(2).toMillis();

    public String getUpdateCron() {
        return updateCron;
    }

    public void setUpdateCron(String updateCron) {
        this.updateCron = updateCron;
    }

    public long getWaitUnitTimeMillis() {
        return waitUnitTimeMillis;
    }

    public void setWaitUnitTimeMillis(long waitUnitTimeMillis) {
        this.waitUnitTimeMillis = waitUnitTimeMillis;
    }

    public long getMinUpdateIntervalMillis() {
        return minUpdateIntervalMillis;
    }

    public void setMinUpdateIntervalMillis(long minUpdateIntervalMillis) {
        this.minUpdateIntervalMillis = minUpdateIntervalMillis;
    }

    public long getNodeKeepAliveMillis() {
        return nodeKeepAliveMillis;
    }

    public void setNodeKeepAliveMillis(long nodeKeepAliveMillis) {
        this.nodeKeepAliveMillis = nodeKeepAliveMillis;
    }
}
