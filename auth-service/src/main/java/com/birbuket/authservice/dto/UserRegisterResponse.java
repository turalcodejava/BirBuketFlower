package com.birbuket.authservice.dto;

import com.birbuket.authservice.enums.Gender;
import com.birbuket.authservice.enums.Role;
import com.birbuket.authservice.enums.UserStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterResponse {

    private Long id;

    private String name;

    private String surname;

    private String email;

    private String username;

    private Gender gender;

    private LocalDate birthDate;

    private Role role;

    private UserStatus status;
}