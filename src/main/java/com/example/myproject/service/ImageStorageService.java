package com.example.myproject.service;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;

@Service
public class ImageStorageService {

    private static final String IMAGE_DIRECTORY = "/path/to/image/directory"; // 이미지 저장 경로

    public String storeImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("파일이 비어있습니다.");
        }

        Path directoryPath = Paths.get(IMAGE_DIRECTORY);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = directoryPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString(); // 저장된 이미지 파일 경로 반환
    }

}
