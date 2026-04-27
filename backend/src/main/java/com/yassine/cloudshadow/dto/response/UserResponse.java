package com.yassine.cloudshadow.dto.response;

import com.yassine.cloudshadow.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private Role role;
    private Long companyId;
    private String companyName;
}