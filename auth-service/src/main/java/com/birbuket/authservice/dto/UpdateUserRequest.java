package com.birbuket.authservice.dto;

import com.birbuket.authservice.enums.Gender;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

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

    @NotNull(message = "Gender seçilməlidir")
    private Gender gender;

    @NotNull(message = "BirthDate boş ola bilməz")
    @Past(message = "BirthDate keçmiş tarix olmalıdır")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate birthDate;
}
