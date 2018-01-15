package com.baswara.generic.web.rest;

import com.baswara.generic.client.AccountFeignClient;
import com.baswara.generic.domain.Flow;
import com.baswara.generic.domain.Layout;
import com.baswara.generic.repository.FlowRepository;
import com.baswara.generic.repository.LayoutRepository;
import com.baswara.generic.service.GenericService;
import com.baswara.generic.service.LayoutService;
import com.mongodb.BasicDBObject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    private final GenericService genericService;
    private final LayoutService layoutService;

    public FlowResource(LayoutRepository layoutRepository, FlowRepository flowRepository, GenericService genericService, LayoutService layoutService, AccountFeignClient accountFeignClient) {
        this.layoutRepository = layoutRepository;
        this.flowRepository = flowRepository;
        this.genericService = genericService;
        this.layoutService = layoutService;
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

    @GetMapping("/layout/{path}")
    public ResponseEntity<Object> layout(HttpServletRequest request, @PathVariable String path) {
        System.out.println("initial Path=" + "layout-" + path);
        Optional<Flow> flowExist = flowRepository.findOneByPath("layout-" + path);
        if (flowExist.isPresent()) {
            return process(request,"layout-" + path, null);
        } else {
            Optional<Layout> layoutExist = layoutRepository.findOneByName(path);
            if (layoutExist.isPresent()) {
                return new ResponseEntity<>(layoutExist.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
            }
        }
    }

    public Object testScript(BasicDBObject param, Map<String, Object> account, String path) {
        Object function = this;
        AccountFeignClient uaaService = null;
        // GenericService genericService
        // LayoutService layoutService
        // -----------------------------------------TEST HERE----------------------------------------------------
        String username = uaaService.registerUser(param);
        if (!username.isEmpty()) {
            BasicDBObject user = new BasicDBObject();
            user.put("userId", param.get("login"));
            user.put("email", param.get("email"));
            user.put("phone", param.get("phone"));
            genericService.saveModel("userProfile", user);
        }
        return username;
    }

    @PostMapping("/process/{path}")
    public ResponseEntity<Object> process(HttpServletRequest request, @PathVariable String path, @RequestBody BasicDBObject param) {
        Optional<Flow> flowExist = flowRepository.findOneByPath(path);
        if (flowExist.isPresent()) {
            Flow flow = flowExist.get();
            Map<String, String[]> queryParamMap = request.getParameterMap();
            Map<String, Object> account = accountFeignClient.getAccount();
            Binding binding = new Binding();
            binding.setVariable("param", param);
            binding.setVariable("queryParam", queryParamMap);
            binding.setVariable("account", account);
            binding.setVariable("function", this);
            binding.setVariable("genericService", genericService);
            binding.setVariable("layoutService", layoutService);
            binding.setVariable("uaaService", accountFeignClient);
            binding.setVariable("path", path);
            GroovyShell shell = new GroovyShell(binding);
            Object returnVal = shell.evaluate(flow.getScript());
            return new ResponseEntity<>(returnVal, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
        }
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
