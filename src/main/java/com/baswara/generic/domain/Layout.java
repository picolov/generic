package com.baswara.generic.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Document(collection = "_layout")
public class Layout implements Serializable {

    @Id
    private String id;

    private String name;

    private String title;

    private String lang;

    private String viewAs;

    private List<Map<String, Object>> init = new ArrayList<>();

    private List<Map<String, Object>> content = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getViewAs() {
        return viewAs;
    }

    public void setViewAs(String viewAs) {
        this.viewAs = viewAs;
    }

    public List<Map<String, Object>> getInit() {
        return init;
    }

    public void setInit(List<Map<String, Object>> init) {
        this.init = init;
    }

    public List<Map<String, Object>> getContent() {
        return content;
    }

    public void setContent(List<Map<String, Object>> content) {
        this.content = content;
    }
}
