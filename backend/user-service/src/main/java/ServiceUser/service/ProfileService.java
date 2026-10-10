package ServiceUser.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import ServiceUser.dto.ProfileDto;
import ServiceUser.exception.GlobalException;
import ServiceUser.models.UserModel;
import ServiceUser.repository.UserRepository;

@Service
public class ProfileService {
    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileDto getUserProfile(String name) {
        UserModel user = userRepository.findByName(name)
                .orElseThrow(() -> new GlobalException("User not found", HttpStatus.NOT_FOUND));
        return mappingToProfileDto(user);
    }

    public ProfileDto mappingToProfileDto(UserModel user) {
        ProfileDto ProfileDto = new ProfileDto();
        ProfileDto.setUserId(user.getId());
        ProfileDto.setName(user.getName());
        ProfileDto.setEmail(user.getEmail());
        ProfileDto.setAvatar(user.getAvatar() == null ? null : "http://localhost:8080/avatars/" + user.getAvatar());
        ProfileDto.setRole(user.getRole());
        return ProfileDto;
    }
}