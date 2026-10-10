package ServiceMedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ServiceMedia.models.MediaModel;

import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<MediaModel, String> {

    // Find avatar for specific user (where productId is null)
    Optional<MediaModel> queryWithOwnerIdAndProductIdIsNull(String ownerId);

    // Find products' images for a specific owner
    java.util.List<MediaModel> findByOwnerIdAndProductIdNotNull(String ownerId);
}