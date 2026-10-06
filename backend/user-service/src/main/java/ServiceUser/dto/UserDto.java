package ServiceUser.dto;

import jakarta.validation.constraints.Pattern;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserDto {
    @Size(min = 3, max = 20, message = "name must be between 3 and 20 characters")
    @NotBlank(message = "name cannot be blank")
    private String name;
    @Size(min = 8, max = 30, message = "Email must be between 8 and 30 characters")
    @NotBlank(message = "Email cannot be blank")
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Email is not valid")
    private String email;
    @Size(min = 8, max = 30, message = "Password must be between 8 and 30 characters")
    @NotBlank(message = "Password cannot be blank")
    private String password;
    @Pattern(regexp = "CLIENT|SELLER", message = "Role must be either CLIENT or SELLER")
    @NotBlank(message = "Role must not be blank")
    private String role;
}