package com.baswara.generic.web.rest;

import com.baswara.generic.domain.UrlRoute;
import com.baswara.generic.repository.UrlRouteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/urlRoute")
public class UrlRouteResource {

    private final UrlRouteRepository urlRouteRepository;

    public UrlRouteResource(UrlRouteRepository urlRouteRepository) {
        this.urlRouteRepository = urlRouteRepository;
    }

    @GetMapping("/count")
    public ResponseEntity<Object> count() {
        long count = urlRouteRepository.count();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String id) {
        boolean result = urlRouteRepository.exists(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<Object> deleteAll() {
        Map<String, Object> result = new HashMap<>();
        urlRouteRepository.deleteAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        urlRouteRepository.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll() {
        List<UrlRoute> result = urlRouteRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable String id) {
        UrlRoute result = urlRouteRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Object> save(@RequestBody UrlRoute objParam) {
        UrlRoute result = urlRouteRepository.save(objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<Object> update(@RequestBody UrlRoute objParam) {
        UrlRoute urlRoute = urlRouteRepository.findOne(objParam.getId());
        urlRoute.setUrl(objParam.getUrl());
        urlRoute.setDescription(objParam.getDescription());
        urlRouteRepository.save(urlRoute);
        return new ResponseEntity<>(urlRoute, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> saveList(@RequestBody List<UrlRoute> objParamList) {
        List<UrlRoute> result = urlRouteRepository.save(objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
