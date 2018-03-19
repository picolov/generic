package com.baswara.generic.web.rest;

import com.baswara.generic.service.ReportService;
import com.mongodb.BasicDBObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportResource {

    private final ReportService reportService;

    public ReportResource(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/download/{id}")
    public ResponseEntity<Object> process(HttpServletRequest request, @PathVariable String id, @RequestBody BasicDBObject param) {
        byte[] reportBytes;
        try {
            reportBytes = reportService.generate(id, param, "pdf");

            ByteArrayResource resource = new ByteArrayResource(reportBytes);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + id + ".pdf");
            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(reportBytes.length)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(new HashMap<String, Map>(), HttpStatus.OK);
        }
    }
}
