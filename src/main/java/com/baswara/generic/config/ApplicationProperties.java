package com.baswara.generic.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Generic.
 * <p>
 * Properties are configured in the application.yml file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {
    private String uploadFolder;

    private String reportFolder;

    public String getReportFolder() {
        return reportFolder;
    }

    public void setReportFolder(String reportFolder) {
        this.reportFolder = reportFolder;
    }

    public String getUploadFolder() {
        return uploadFolder;
    }

    public void setUploadFolder(String uploadFolder) {
        this.uploadFolder = uploadFolder;
    }

}
