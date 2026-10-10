package ServiceUser.dto;

import ServiceUser.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProfileDto {
    private String userId;
    private String name;
    private String email;
    private String avatar;
    private Role role;
}