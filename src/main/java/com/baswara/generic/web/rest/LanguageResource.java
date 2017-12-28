package com.baswara.generic.web.rest;

import com.baswara.generic.domain.Language;
import com.baswara.generic.repository.LanguageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/language")
public class LanguageResource {

    private final LanguageRepository languageRepository;

    public LanguageResource(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @GetMapping("/count")
    public ResponseEntity<Object> count() {
        long count = languageRepository.count();
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/exist/{id}")
    public ResponseEntity<Object> existsById(@PathVariable String id) {
        boolean result = languageRepository.exists(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("")
    public ResponseEntity<Object> deleteAll() {
        Map<String, Object> result = new HashMap<>();
        languageRepository.deleteAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteById(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        languageRepository.delete(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll() {
        List<Language> result = languageRepository.findAll();
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable String id) {
        Language result = languageRepository.findOne(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Object> findByName(@PathVariable String name) {
        Optional<Language> languageExist = languageRepository.findOneByName(name);
        if (languageExist.isPresent()) {
            return new ResponseEntity<>(languageExist.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
        }
    }

    @PostMapping("")
    public ResponseEntity<Object> save(@RequestBody Language objParam) {
        Language result = languageRepository.save(objParam);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<Object> update(@RequestBody Language objParam) {
        Language language = languageRepository.findOne(objParam.getId());
        language.setName(objParam.getName());
        language.setContent(objParam.getContent());
        languageRepository.save(language);
        return new ResponseEntity<>(language, HttpStatus.OK);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Object> saveList(@RequestBody List<Language> objParamList) {
        List<Language> result = languageRepository.save(objParamList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
