package ServiceMedia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaResponse {
    private String id;
    private String imagePath;
    private String ownerId;
    private String productId;
    private String mediaType;
}