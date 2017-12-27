package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Flow;
import com.baswara.generic.domain.Layout;
import com.baswara.generic.repository.FlowRepository;
import com.baswara.generic.repository.LayoutRepository;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/flow")
public class FlowResource {

    private final LayoutRepository layoutRepository;

    private final FlowRepository flowRepository;

    public FlowResource(LayoutRepository layoutRepository, FlowRepository flowRepository) {
        this.layoutRepository = layoutRepository;
        this.flowRepository = flowRepository;
    }

    @DeleteMapping("")
    public ResponseEntity<Object> deleteAll() {
        Map<String, Object> result = new HashMap<>();
        flowRepository.deleteAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        flowRepository.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll() {
        List<Flow> result = flowRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable String id) {
        Flow result = flowRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/path/{path}")
    public ResponseEntity<Object> findPath(@PathVariable String path) {
        Optional<Flow> flowExist = flowRepository.findOneByPath(path);
        Binding binding = new Binding();
        binding.setVariable("res", this);
        binding.setVariable("path", path);
        GroovyShell shell = new GroovyShell(binding);
        String groovyScript = "return res.testong(path)";
        Object value = shell.evaluate(groovyScript);
        int a = 1;
        if (flowExist.isPresent()) {
            return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
        } else {
            Optional<Layout> layoutExist = layoutRepository.findOneByName(path);
            if (layoutExist.isPresent()) {
                return new ResponseEntity<>(layoutExist.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
            }
        }
    }

    public String testong (String path) {
        String result = "none";
        Optional<Layout> layoutExist = layoutRepository.findOneByName(path);
        if (layoutExist.isPresent()) result = layoutExist.get().getId();
        return result;
    }

    @PostMapping("")
    public ResponseEntity<Object> save(@RequestBody Flow objParam) {
        Flow result = flowRepository.save(objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<Object> update(@RequestBody Flow objParam) {
        Flow flow = flowRepository.findOne(objParam.getId());
        flow.setPath(objParam.getPath());
        flow.setScript(objParam.getScript());
        flowRepository.save(flow);
        return new ResponseEntity<>(flow, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> saveList(@RequestBody List<Flow> objParamList) {
        List<Flow> result = flowRepository.save(objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
