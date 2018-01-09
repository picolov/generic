package com.baswara.generic.service;

import com.baswara.generic.domain.Layout;
import com.baswara.generic.repository.LayoutRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class LayoutService {

    private final Logger log = LoggerFactory.getLogger(LayoutService.class);

    private final LayoutRepository layoutRepository;

    public LayoutService(LayoutRepository layoutRepository) {
        this.layoutRepository = layoutRepository;
    }

    public Layout findByName(String name) {
        Optional<Layout> layoutExist = layoutRepository.findOneByName(name);
        if (layoutExist.isPresent()) {
            return layoutExist.get();
        } else {
            return null;
        }
    }

}
