package com.deploytrack.dto;

import com.deploytrack.enums.UserRole;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private UserRole role;
}
