package ServiceUser.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ServiceUser.dto.ResponseDto;
import ServiceUser.dto.UserDto;
import ServiceUser.enums.Role;
import ServiceUser.exception.GlobalException;
import ServiceUser.models.UserModel;
import ServiceUser.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseDto registerUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new GlobalException("Email is already in use", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByName(userDto.getName())) {
            throw new GlobalException("name is already in use", HttpStatus.CONFLICT);
        }
        UserModel user = new UserModel();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setRole(Role.valueOf(userDto.getRole()));
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);
        return new ResponseDto("User registered successfully");
    }
}