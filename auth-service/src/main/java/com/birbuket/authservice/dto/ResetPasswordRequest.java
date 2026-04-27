package com.birbuket.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Token boş ola bilməz")
    private String token;

    @NotBlank(message = "Yeni parol boş ola bilməz")
    @Size(min = 8, max = 100, message = "Parol ən azı 8 simvol olmalıdır")
    private String newPassword;

    @NotBlank(message = "Parol təsdiqi boş ola bilməz")
    @Size(min = 8, max = 100, message = "Parol təsdiqi ən azı 8 simvol olmalıdır")
    private String confirmPassword;
}
