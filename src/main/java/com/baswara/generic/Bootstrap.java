package com.baswara.generic;

import com.baswara.generic.domain.Layout;
import com.baswara.generic.domain.Meta;
import com.baswara.generic.repository.LayoutRepository;
import com.baswara.generic.repository.MetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Bootstrap implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private final MetaRepository metaRepository;
    private final LayoutRepository layoutRepository;

    public Bootstrap(MetaRepository metaRepository, LayoutRepository layoutRepository) {
        this.metaRepository = metaRepository;
        this.layoutRepository = layoutRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Optional<Meta> metaExist = metaRepository.findOneByName("person");
        if (!metaExist.isPresent()) {
            log.info("Bootstrapping Meta...");
            Meta meta = new Meta();
            meta.setName("person");
            Map<String, Map<String, Object>> columnMap = new HashMap<>();
            Map<String, Object> columnUnitMap = new HashMap<>();
            columnUnitMap.put("name", "name");
            columnUnitMap.put("type", "string");
            columnMap.put("name", columnUnitMap);
            columnUnitMap = new HashMap<>();
            columnUnitMap.put("name", "age");
            columnUnitMap.put("type", "numeric");
            columnMap.put("age", columnUnitMap);
            meta.setColumns(columnMap);
            metaRepository.save(meta);
        }

        Optional<Layout> layoutExist = layoutRepository.findOneByName("personPage");
        if (!layoutExist.isPresent()) {
            log.info("Bootstrapping Layout...");
            Layout layout = new Layout();
            layout.setName("personPage");
            List<Map<String, Object>> componentList = new ArrayList<>();
            Map<String, Object> componentUnitMap = new HashMap<>();
            componentUnitMap.put("row", 1);
            componentUnitMap.put("col", 1);
            componentUnitMap.put("width", 2);
            componentUnitMap.put("type", "label");
            componentUnitMap.put("text", "person.name");
            componentList.add(componentUnitMap);
            componentUnitMap = new HashMap<>();
            componentUnitMap.put("row", 1);
            componentUnitMap.put("col", 3);
            componentUnitMap.put("width", 10);
            componentUnitMap.put("type", "textfield");
            componentUnitMap.put("model", "person.name");
            componentList.add(componentUnitMap);
            componentUnitMap = new HashMap<>();
            componentUnitMap.put("row", 2);
            componentUnitMap.put("col", 1);
            componentUnitMap.put("width", 2);
            componentUnitMap.put("type", "label");
            componentUnitMap.put("text", "person.age");
            componentList.add(componentUnitMap);
            componentUnitMap = new HashMap<>();
            componentUnitMap.put("row", 2);
            componentUnitMap.put("col", 2);
            componentUnitMap.put("width", 10);
            componentUnitMap.put("type", "textfield");
            componentUnitMap.put("text", "person.age");
            componentList.add(componentUnitMap);
            layout.setContent(componentList);
            layoutRepository.save(layout);
        }
        log.info("...Bootstrapping completed");
    }

}
