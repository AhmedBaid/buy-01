package ServiceMedia.service;

import org.apache.tika.Tika;
import org.apache.tika.io.TikaInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ServiceMedia.models.MediaModel;
import ServiceMedia.repository.MediaRepository;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;

    @Value("${media.upload.directory:uploads}")
    private String uploadDirectory;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2 MB
    private static final String[] ALLOWED_MEDIA_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
            "image/svg+xml", "image/bmp"
    };
    private static final Tika tika = new Tika();

    public static boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Check MIME type using Tika
        try {
            String detectedType = tika.detect(file.getInputStream());
            return isAllowedMediaType(detectedType);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isAllowedMediaType(String detectedType) {
        if (detectedType == null) {
            return false;
        }

        for (String allowedType : ALLOWED_MEDIA_TYPES) {
            if (detectedType.toLowerCase().contains(allowedType.toLowerCase()) ||
                detectedType.equalsIgnoreCase(allowedType)) {
                return true;
            }
        }
        return false;
    }

    public MediaModel uploadMedia(MultipartFile file, String ownerId, String productId) throws IOException {
        // Validate image
        if (!isValidImage(file)) {
            throw new IllegalArgumentException("Invalid image file: must be image/* with size ≤ 2 MB");
        }

        // Create upload directory if it doesn't exist
        File directory = new File(uploadDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = System.currentTimeMillis() + "_" + originalFilename;
        Path filePath = new File(directory, uniqueFilename).toPath();

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create media record
        MediaModel mediaModel = new MediaModel();
        mediaModel.setImagePath(filePath.toString());
        mediaModel.setOwnerId(ownerId);
        mediaModel.setProductId(productId);

        // Detect and store media type
        TikaInputStream tikaInputStream = TikaInputStream.get(file.getInputStream());
        String detectedType = tika.detect(tikaInputStream);
        mediaModel.setMediaType(detectedType);

        // Save to database
        return mediaRepository.save(mediaModel);
    }

    public MediaModel getMediaById(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Media not found with id: " + id));
    }

    public void deleteMedia(String id) {
        MediaModel mediaModel = getMediaById(id);
        mediaRepository.delete(mediaModel);
    }
}