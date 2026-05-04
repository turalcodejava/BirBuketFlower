package com.birbuket.authservice.dto;

import com.birbuket.authservice.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    @NotBlank(message = "Name boş ola bilməz")
    @Size(min = 2, max = 50, message = "Name 2-50 simvol arası olmalıdır")
    @Pattern(regexp = "^[A-Za-zƏəĞğİıÖöŞşÜüÇç\\s]+$", message = "Name yalnız hərflərdən ibarət olmalıdır")
    private String name;

    @NotBlank(message = "Surname boş ola bilməz")
    @Size(min = 2, max = 50, message = "Surname 2-50 simvol arası olmalıdır")
    @Pattern(regexp = "^[A-Za-zƏəĞğİıÖöŞşÜüÇç\\s]+$", message = "Surname yalnız hərflərdən ibarət olmalıdır")
    private String surname;

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda olmalıdır")
    private String email;

    @NotBlank(message = "Phone number boş ola bilməz")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "Telefon nömrəsi +994XXXXXXXXX formatında olmalıdır")
    private String phoneNumber;

    @NotBlank(message = "Username boş ola bilməz")
    @Size(min = 3, max = 50, message = "Username 3-50 simvol arası olmalıdır")
    private String username;

    @NotBlank(message = "Password boş ola bilməz")
    @Size(min = 8, max = 100, message = "Password ən azı 8 simvol olmalıdır")
    private String password;

    @NotBlank(message = "Confirm password boş ola bilməz")
    @Size(min = 8, max = 100, message = "Confirm password ən azı 8 simvol olmalıdır")
    private String confirmPassword;

    @NotNull(message = "Gender seçilməlidir")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @NotNull(message = "BirthDate boş ola bilməz")
    @Past(message = "BirthDate keçmiş tarix olmalıdır")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;
}