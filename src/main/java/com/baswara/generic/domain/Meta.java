package com.baswara.generic.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Document(collection = "_meta")
public class Meta implements Serializable {

    @Id
    private String id;

    private String name;

    private Map<String, Map<String, Object>> columns = new HashMap<>();

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

    public Map<String, Map<String, Object>> getColumns() {
        return columns;
    }

    public void setColumns(Map<String,  Map<String, Object>> columns) {
        this.columns = columns;
    }
}
