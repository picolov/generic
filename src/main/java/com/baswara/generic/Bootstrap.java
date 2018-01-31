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
        log.info("...Bootstrapping completed");
    }

}
