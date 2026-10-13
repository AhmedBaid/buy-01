package ServiceMedia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ServiceMedia.dto.MediaResponse;
import ServiceMedia.models.MediaModel;
import ServiceMedia.service.ProductImageService;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/media/products")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<MediaResponse>> getProductImagesByOwner(@PathVariable String ownerId) {
        List<MediaModel> images = productImageService.getProductImagesByOwner(ownerId);

        List<MediaResponse> responses = images.stream()
                .map(model -> new MediaResponse(
                        model.getId(),
                        model.getImagePath(),
                        model.getOwnerId(),
                        model.getProductId(),
                        model.getMediaType()
                ))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getProductImage(@PathVariable String id) {
        try {
            MediaModel mediaModel = productImageService.getProductImageById(id);
            java.nio.file.Path imagePath = java.nio.file.Paths.get(mediaModel.getImagePath());

            // Check if file exists
            if (!java.nio.file.Files.exists(imagePath)) {
                return ResponseEntity.notFound().build();
            }

            // Read file content
            byte[] imageBytes = java.nio.file.Files.readAllBytes(imagePath);

            // Determine MIME type
            String mimeType = java.nio.file.Files.probeContentType(imagePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            // Set caching headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(mimeType));
            headers.setContentLength(imageBytes.length);
            headers.setCacheControl("public, max-age=3600"); // Cache for 1 hour

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(imageBytes);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductImage(@PathVariable String id) {
        productImageService.deleteProductImage(id);
        return ResponseEntity.noContent().build();
    }
}