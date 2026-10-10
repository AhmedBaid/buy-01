package ServiceMedia.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ServiceMedia.dto.MediaResponse;
import ServiceMedia.service.MediaService;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/media/images")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerId") String ownerId,
            @RequestParam(value = "productId", required = false) String productId) {

        try {
            MediaModel mediaModel = mediaService.uploadMedia(file, ownerId, productId);

            MediaResponse response = new MediaResponse(
                mediaModel.getId(),
                mediaModel.getImagePath(),
                mediaModel.getOwnerId(),
                mediaModel.getProductId(),
                mediaModel.getMediaType()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        try {
            MediaModel mediaModel = mediaService.getMediaById(id);
            Path imagePath = Paths.get(mediaModel.getImagePath());

            // Check if file exists
            if (!Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            // Read file content
            byte[] imageBytes = Files.readAllBytes(imagePath);

            // Determine MIME type
            String mimeType = Files.probeContentType(imagePath);
            if (mimeType == null) {
                mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            // Set caching headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mimeType));
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("public, max-age=3600"); // Cache for 1 hour

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        mediaService.deleteMedia(id);
        return ResponseEntity.noContent().build();
    }
}