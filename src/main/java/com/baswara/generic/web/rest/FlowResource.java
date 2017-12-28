package com.baswara.generic.web.rest;

import com.baswara.generic.client.AccountFeignClient;
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

    private final AccountFeignClient accountFeignClient;

    private final LayoutRepository layoutRepository;

    private final FlowRepository flowRepository;

    public FlowResource(LayoutRepository layoutRepository, FlowRepository flowRepository, AccountFeignClient accountFeignClient) {
        this.layoutRepository = layoutRepository;
        this.flowRepository = flowRepository;
        this.accountFeignClient = accountFeignClient;
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
        System.out.println("initial Path=" + path);
        Optional<Flow> flowExist = flowRepository.findOneByPath(path);
        if (flowExist.isPresent()) {
            Flow flow = flowExist.get();
            Map<String, Object> account = accountFeignClient.getAccount();
            Binding binding = new Binding();
            binding.setVariable("account", account);
            binding.setVariable("function", this);
            binding.setVariable("path", path);
            GroovyShell shell = new GroovyShell(binding);
            String viewPath = shell.evaluate(flow.getScript()).toString();
            System.out.println("Result Path=" + viewPath);
            Optional<Layout> layoutExist = layoutRepository.findOneByName(viewPath);
            if (layoutExist.isPresent()) {
                return new ResponseEntity<>(layoutExist.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
            }
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
