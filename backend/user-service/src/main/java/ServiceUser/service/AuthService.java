package ServiceUser.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ServiceUser.config.JwtProvider;
import ServiceUser.dto.LoginUserDto;
import ServiceUser.dto.RegisterUserDto;
import ServiceUser.dto.ResponseDto;
import ServiceUser.enums.Role;
import ServiceUser.exception.GlobalException;
import ServiceUser.models.UserModel;
import ServiceUser.repository.UserRepository;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public ResponseDto registerUser(RegisterUserDto registerUserDto) {
        if (userRepository.existsByEmail(registerUserDto.getEmail())) {
            throw new GlobalException("Email is already in use", HttpStatus.CONFLICT);
        }
        if (userRepository.existsByName(registerUserDto.getName())) {
            throw new GlobalException("name is already in use", HttpStatus.CONFLICT);
        }
        UserModel user = new UserModel();
        user.setName(registerUserDto.getName());
        user.setEmail(registerUserDto.getEmail());
        user.setRole(Role.valueOf(registerUserDto.getRole()));
        user.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        userRepository.save(user);
        return new ResponseDto("User registered successfully");
    }

    public ResponseDto loginUser(LoginUserDto LoginUserDto) {
        UserModel user = findUser(LoginUserDto.getNameOrEmail());
        if (!passwordEncoder.matches(LoginUserDto.getPassword(), user.getPassword())) {
            throw new GlobalException("Invalid email/username or password", HttpStatus.UNAUTHORIZED);
        }
        String token = jwtProvider.generateToken(user.getId(), user.getName(), user.getRole().toString());
        return new ResponseDto(token);
    }

    private UserModel findUser(String emailOrUsername) {
        return userRepository.findByNameOrEmail(emailOrUsername, emailOrUsername)
                .orElseThrow(() -> new GlobalException("Invalid email/username or password", HttpStatus.UNAUTHORIZED));
    }
}