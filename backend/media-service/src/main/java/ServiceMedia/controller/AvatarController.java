package ServiceMedia.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ServiceMedia.dto.MediaResponse;
import ServiceMedia.service.AvatarService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/media/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MediaResponse> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerId") String ownerId) {

        try {
            MediaModel mediaModel = avatarService.uploadAvatar(file, ownerId);

            MediaResponse response = new MediaResponse(
                mediaModel.getId(),
                mediaModel.getImagePath(),
                mediaModel.getOwnerId(),
                null,
                mediaModel.getMediaType()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<MediaResponse> getAvatar(@PathVariable String ownerId) {
        MediaResponse response = avatarService.getAvatarByOwner(ownerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/owner/{ownerId}")
    public ResponseEntity<Void> deleteAvatar(@PathVariable String ownerId) {
        avatarService.deleteAvatar(ownerId);
        return ResponseEntity.noContent().build();
    }
}