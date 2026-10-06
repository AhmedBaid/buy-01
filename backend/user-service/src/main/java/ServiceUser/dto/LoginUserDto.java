package ServiceUser.dto;

import lombok.Getter;

@Getter
public class LoginUserDto {
    String NameOrEmail;
    String password;
}