package com.birbuket.authservice.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequest {

    @NotBlank(message = "Username boş ola bilməz")
    private String username;
    @NotBlank(message = "Password boş ola bilməz")
    private String password;
}