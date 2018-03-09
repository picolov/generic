package com.baswara.generic.service;

import com.baswara.generic.config.ApplicationProperties;
import com.baswara.generic.domain.UploadFiles;
import com.baswara.generic.repository.UploadFilesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileService {

    private final UploadFilesRepository uploadFilesRepository;
    private final ApplicationProperties applicationProperties;

    private final Logger log = LoggerFactory.getLogger(FileService.class);

    public FileService(ApplicationProperties applicationProperties, UploadFilesRepository uploadFilesRepository) {
        this.applicationProperties = applicationProperties;
        this.uploadFilesRepository = uploadFilesRepository;
    }

    public List<String> saveUploadedFiles(List<MultipartFile> files) throws IOException {
        List<String> idList = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue; //next pls
            }
            String fileId = UUID.randomUUID().toString();
            String[] fileNameToken = file.getOriginalFilename().split("\\.");
            byte[] bytes = file.getBytes();
            Path path = Paths.get(applicationProperties.getUploadFolder() + fileId + "." + fileNameToken[1]);
            Files.write(path, bytes);
            UploadFiles uploadFiles = new UploadFiles();
            uploadFiles.setId(fileId);
            uploadFiles.setFileName(fileNameToken[0]);
            uploadFiles.setExtension(fileNameToken[1]);
            uploadFiles.setSize(file.getSize());
            uploadFiles.setFilePath(applicationProperties.getUploadFolder() + fileId + "." + fileNameToken[1]);
            uploadFiles.setDescription("");
            uploadFilesRepository.save(uploadFiles);
            idList.add(fileId);
        }
        return idList;
    }

    public List<String> saveUploadedBase64(List<String> base64Images) throws IOException {
        List<String> idList = new ArrayList<>();
        for (String base64Image : base64Images) {
            String fileType = "jpg";
            if (base64Image.startsWith("data:")) {
                String[] base64ImageToken = base64Image.split(",");
                String fileTypeDesc = base64ImageToken[0].split(";")[0].substring(5);
                switch (fileTypeDesc) {
                    case "image/jpeg":
                    case "image/jpg":
                        fileType = "jpg";
                        break;
                    case "image/gif":
                        fileType = "png";
                        break;
                    default:
                        fileType = "jpg";
                }
                base64Image = base64ImageToken[1];
            }
            byte[] binaryImage = Base64.getDecoder().decode(base64Image.getBytes(StandardCharsets.UTF_8));
            String fileId = UUID.randomUUID().toString();
            Path path = Paths.get(applicationProperties.getUploadFolder() + fileId + "." + fileType);
            Files.write(path, binaryImage);
            UploadFiles uploadFiles = new UploadFiles();
            uploadFiles.setId(fileId);
            uploadFiles.setFileName(fileId);
            uploadFiles.setExtension(fileType);
            uploadFiles.setSize(binaryImage.length);
            uploadFiles.setFilePath(applicationProperties.getUploadFolder() + fileId + "." + fileType);
            uploadFiles.setDescription("");
            uploadFilesRepository.save(uploadFiles);
            idList.add(fileId);
        }
        return idList;
    }

}
