package ServiceMedia.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import ServiceMedia.models.MediaModel;
import ServiceMedia.repository.MediaRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final MediaRepository mediaRepository;

    public List<MediaModel> getProductImagesByOwner(String ownerId) {
        return mediaRepository.findByOwnerIdAndProductIdNotNull(ownerId);
    }

    public MediaModel getProductImageById(String id) {
        return mediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product image not found with id: " + id));
    }

    public void deleteProductImage(String id) {
        MediaModel productImage = getProductImageById(id);
        mediaRepository.delete(productImage);
    }
}