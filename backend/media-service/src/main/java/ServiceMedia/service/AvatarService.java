package ServiceMedia.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ServiceMedia.models.MediaModel;
import ServiceMedia.repository.MediaRepository;
import ServiceMedia.dto.MediaResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AvatarService {

    private final MediaService mediaService;
    private final MediaRepository mediaRepository;

    public MediaModel uploadAvatar(MultipartFile file, String ownerId) throws IOException {
        // Validate avatar size and type
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Avatar file size must be ≤ 2 MB");
        }

        String[] allowedTypes = {"image/jpeg", "image/jpg", "image/png", "image/gif"};
        String contentType = file.getContentType();
        if (contentType == null || !java.util.Arrays.asList(allowedTypes).contains(contentType)) {
            throw new IllegalArgumentException("Avatar must be one of: JPEG, PNG, GIF");
        }

        // Upload media with null productId (avatar has no product)
        return mediaService.uploadMedia(file, ownerId, null);
    }

    public MediaResponse getAvatarByOwner(String ownerId) {
        // Find user's avatar (with null productId)
        Optional<MediaModel> mediaModel = mediaRepository.findByOwnerIdAndProductIdIsNull(ownerId);

        return mediaModel.map(model -> new MediaResponse(
            model.getId(),
            model.getImagePath(),
            model.getOwnerId(),
            null,
            model.getMediaType()
        )).orElseThrow(() -> new RuntimeException("Avatar not found for owner: " + ownerId));
    }

    public void deleteAvatar(String ownerId) {
        Optional<MediaModel> avatar = mediaRepository.findByOwnerIdAndProductIdIsNull(ownerId);

        avatar.ifPresent(model -> {
            // Delete file from disk
            try {
                Path filePath = Paths.get(model.getImagePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log error but still delete from database
            }

            // Delete from database
            mediaRepository.delete(model);
        });
    }
}