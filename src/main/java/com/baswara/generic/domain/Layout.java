package com.baswara.generic.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

    private List<Map<String, Object>> init = new ArrayList<>();

    private Map<String, Object> container = new HashMap<>();

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

    public List<Map<String, Object>> getInit() {
        return init;
    }

    public void setInit(List<Map<String, Object>> init) {
        this.init = init;
    }

    public Map<String, Object> getContainer() {
        return container;
    }

    public void setContainer(Map<String, Object> container) {
        this.container = container;
    }
}
