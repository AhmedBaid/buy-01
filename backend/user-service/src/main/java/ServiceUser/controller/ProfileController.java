package ServiceUser.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ServiceUser.dto.ProfileDto;
import ServiceUser.service.ProfileService;

@RestController
@RequestMapping("/api")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileDto> getUserProfile(Principal principal) {
        String name = principal.getName();
        return ResponseEntity.ok(profileService.getUserProfile(name));
    }
}