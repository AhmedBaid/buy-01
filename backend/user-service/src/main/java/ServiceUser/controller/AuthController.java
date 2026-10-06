package ServiceUser.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ServiceUser.dto.ResponseDto;
import ServiceUser.dto.LoginUserDto;
import ServiceUser.dto.RegisterUserDto;
import ServiceUser.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService userService;

    public AuthController(AuthService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto> registerUser(@RequestBody RegisterUserDto userDto) {
        return ResponseEntity.ok(userService.registerUser(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDto> loginUser(@RequestBody LoginUserDto userDto) {
        return ResponseEntity.ok(userService.loginUser(userDto));
    }
}