package ServiceMedia.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ServiceMedia.models.MediaModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepository extends MongoRepository<MediaModel, String> {

    Optional<MediaModel> findByOwnerIdAndProductIdIsNull(String ownerId);

    List<MediaModel> findByOwnerIdAndProductIdNotNull(String ownerId);

    
}