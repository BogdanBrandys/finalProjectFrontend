package com.kodilla.final_project_frontend.shared;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {
    private String first_name;
    private String last_name;
    private String email;
    private String username;
    private List<RoleDTO> roles;

}
