package com.birbuket.authservice.models;

import com.birbuket.authservice.enums.Gender;
import com.birbuket.authservice.enums.Role;
import com.birbuket.authservice.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name boş ola bilməz")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-zƏəĞğİıÖöŞşÜüÇç\\s]+$", message = "Name yalnız hərflərdən ibarət olmalıdır")
    @Column(nullable = false, length = 50)
    private String name;

    @NotBlank(message = "Surname boş ola bilməz")
    @Size(min = 2, max = 50)
    @Pattern(regexp = "^[A-Za-zƏəĞğİıÖöŞşÜüÇç\\s]+$", message = "Surname yalnız hərflərdən ibarət olmalıdır")
    @Column(nullable = false, length = 50)
    private String surname;

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda olmalıdır")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Phone number boş ola bilməz")
    @Pattern(regexp = "^\\+994[0-9]{9}$", message = "Telefon nömrəsi +994XXXXXXXXX formatında olmalıdır")
    @Column(nullable = false, unique = true, length = 13)
    private String phoneNumber;

    @NotBlank(message = "Username boş ola bilməz")
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Password boş ola bilməz")
    @Size(min = 8, max = 100, message = "Password ən azı 8 simvol olmalıdır")
    @Column(nullable = false, length = 100)
    private String password;

    @NotNull(message = "Gender seçilməlidir")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Past(message = "BirthDate keçmiş tarix olmalıdır")
    private LocalDate birthDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Addresses> addresses = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(status);
    }
}