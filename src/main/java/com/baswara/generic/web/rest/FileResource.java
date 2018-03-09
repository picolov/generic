package com.baswara.generic.web.rest;

import com.baswara.generic.domain.UploadFiles;
import com.baswara.generic.repository.UploadFilesRepository;
import com.baswara.generic.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/file")
public class FileResource {

    private final FileService fileService;
    private final UploadFilesRepository uploadFilesRepository;

    public FileResource(FileService fileService, UploadFilesRepository uploadFilesRepository) {
        this.fileService = fileService;
        this.uploadFilesRepository = uploadFilesRepository;
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        UploadFiles file = uploadFilesRepository.findOne(id);
        Path path = Paths.get(file.getFilePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName() + "." + file.getExtension());
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(file.getSize())
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body(resource);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> view(@PathVariable String id) throws IOException {
        UploadFiles file = uploadFilesRepository.findOne(id);
        Path path = Paths.get(file.getFilePath());
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));
        HttpHeaders headers = new HttpHeaders();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        switch (file.getExtension().toLowerCase()) {
            case "jpg":
            case "jpeg":
            case "bmp":
                mediaType = MediaType.IMAGE_JPEG;
                break;
            case "png":
                mediaType = MediaType.IMAGE_PNG;
                break;
            case "gif":
                mediaType = MediaType.IMAGE_GIF;
                break;
        }
        return ResponseEntity.ok()
            .headers(headers)
            .contentLength(file.getSize())
            .contentType(mediaType)
            .body(resource);
    }

    @GetMapping("/base64/{id}")
    public ResponseEntity<?> base64(@PathVariable String id) throws IOException {
        Map<String, Object> result = new HashMap<>();
        UploadFiles file = uploadFilesRepository.findOne(id);
        Path path = Paths.get(file.getFilePath());
        String encodedFile = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
        result.put("id", id);
        result.put("base64", encodedFile);
        result.put("filename", file.getFileName());
        result.put("extension", file.getExtension());
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 3.1.1 Single file upload
    @PostMapping("/upload")
    // If not @RestController, uncomment this
    //@ResponseBody
    public ResponseEntity<?> uploadFile(
        @RequestParam("file") MultipartFile uploadfile) {
        Map<String, Object> result = new HashMap<>();
        List<String> idList = new ArrayList<>();
        if (uploadfile.isEmpty()) {
            return new ResponseEntity<>("please select a file!", HttpStatus.OK);
        }
        try {
            idList = fileService.saveUploadedFiles(Arrays.asList(uploadfile));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        result.put("message", "file " + uploadfile.getOriginalFilename() + " successfully uploaded");
        result.put("idList", idList);
        return new ResponseEntity<>(result, HttpStatus.OK);

    }

    // 3.1.2 Multiple file upload
    @PostMapping("/upload/multi")
    public ResponseEntity<?> uploadFileMulti(
        @RequestParam("extraField") String extraField,
        @RequestParam("files") MultipartFile[] uploadfiles) {
        Map<String, Object> result = new HashMap<>();
        List<String> idList = new ArrayList<>();
        // Get file name
        String uploadedFileName = Arrays.stream(uploadfiles).map(x -> x.getOriginalFilename()).filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));
        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity<>("please select a file!", HttpStatus.OK);
        }
        try {
            idList = fileService.saveUploadedFiles(Arrays.asList(uploadfiles));
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        result.put("message", "Successfully uploaded - " + uploadedFileName);
        result.put("idList", idList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
