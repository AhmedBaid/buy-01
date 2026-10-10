package ServiceUser.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import ServiceUser.models.UserModel;

@Repository
public interface UserRepository extends MongoRepository<UserModel, String> {
    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Optional<UserModel> findByNameOrEmail(String name, String email);

    Optional<UserModel> findByName(String name);

}