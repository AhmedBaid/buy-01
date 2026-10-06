package ServiceUser.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import ServiceUser.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Document(collection = "users")
@Getter
@Setter
public class UserModel {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private Role role = Role.CLIENT;
    private String avatar;
}